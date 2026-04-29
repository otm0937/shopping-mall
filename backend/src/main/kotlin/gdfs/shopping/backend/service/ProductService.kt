package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import gdfs.shopping.backend.dto.product.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

@Service
class ProductService(
    private val productRepository: ProductRepository,
    @Value("\${app.upload.dir:uploads/products}") private val uploadDir: String
) {

    private val allowedImageExtensions = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
        "image/gif" to "gif"
    )

    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductListResponse> {
        return productRepository.findAll().map { product ->
            ProductListResponse(
                id = product.id!!,
                name = product.name,
                price = product.price,
                imageUrl = product.imagePath?.let { "/uploads/products/${it.substringAfterLast("/")}" }
            )
        }
    }

    @Transactional(readOnly = true)
    fun getProduct(id: Long): ProductDetailResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("상품을 찾을 수 없습니다: $id") }

        return ProductDetailResponse(
            id = product.id!!,
            name = product.name,
            price = product.price,
            description = product.description,
            imageUrl = product.imagePath?.let { "/uploads/products/${it.substringAfterLast("/")}" }
        )
    }

    @Transactional
    fun createProduct(request: ProductCreateRequest, image: MultipartFile?): ProductResponse {
        val imagePath = image?.let { saveImage(it) }

        val product = Product(
            name = request.name,
            price = request.price,
            description = request.description,
            imagePath = imagePath
        )

        val savedProduct = productRepository.save(product)

        return ProductResponse(
            id = savedProduct.id!!,
            name = savedProduct.name,
            price = savedProduct.price,
            description = savedProduct.description,
            imageUrl = savedProduct.imagePath?.let { "/uploads/products/${it.substringAfterLast("/")}" },
            message = "상품이 생성되었습니다"
        )
    }

    @Transactional
    fun updateProduct(id: Long, request: ProductUpdateRequest, image: MultipartFile?): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("상품을 찾을 수 없습니다: $id") }

        product.name = request.name
        product.price = request.price
        product.description = request.description

        val oldImagePath = product.imagePath
        val newImagePath = image?.let { saveImage(it) }

        if (newImagePath != null) {
            product.imagePath = newImagePath
        }

        val updatedProduct = productRepository.save(product)

        if (newImagePath != null && oldImagePath != null) {
            deleteImage(oldImagePath)
        }

        return ProductResponse(
            id = updatedProduct.id!!,
            name = updatedProduct.name,
            price = updatedProduct.price,
            description = updatedProduct.description,
            imageUrl = updatedProduct.imagePath?.let { "/uploads/products/${it.substringAfterLast("/")}" },
            message = "상품이 수정되었습니다"
        )
    }

    @Transactional
    fun deleteProduct(id: Long) {
        val product = productRepository.findById(id)
            .orElseThrow { IllegalArgumentException("상품을 찾을 수 없습니다: $id") }

        product.imagePath?.let { deleteImage(it) }
        productRepository.delete(product)
    }

    private fun saveImage(image: MultipartFile): String {
        val contentType = image.contentType?.lowercase()
        val extension = allowedImageExtensions[contentType]
            ?: throw IllegalArgumentException("지원하지 않는 이미지 형식입니다. JPG, PNG, WebP, GIF 파일만 업로드할 수 있습니다.")

        if (image.isEmpty) {
            throw IllegalArgumentException("빈 이미지 파일은 업로드할 수 없습니다.")
        }

        if (!hasValidImageSignature(image, extension)) {
            throw IllegalArgumentException("이미지 파일의 내용이 형식과 일치하지 않습니다.")
        }

        val uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize()
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath)
        }

        val filename = "${UUID.randomUUID()}.${extension}"
        val filePath = uploadPath.resolve(filename).normalize()

        if (!filePath.startsWith(uploadPath)) {
            throw IllegalArgumentException("이미지 저장 경로가 올바르지 않습니다.")
        }

        image.inputStream.use { input ->
            Files.copy(input, filePath)
        }

        return filePath.toString()
    }

    private fun hasValidImageSignature(image: MultipartFile, extension: String): Boolean {
        val header = image.inputStream.use { input -> input.readNBytes(12) }

        return when (extension) {
            "jpg" -> header.size >= 3 &&
                header[0] == 0xFF.toByte() &&
                header[1] == 0xD8.toByte() &&
                header[2] == 0xFF.toByte()
            "png" -> header.size >= 8 &&
                header[0] == 0x89.toByte() &&
                header[1] == 0x50.toByte() &&
                header[2] == 0x4E.toByte() &&
                header[3] == 0x47.toByte() &&
                header[4] == 0x0D.toByte() &&
                header[5] == 0x0A.toByte() &&
                header[6] == 0x1A.toByte() &&
                header[7] == 0x0A.toByte()
            "webp" -> header.size >= 12 &&
                String(header.copyOfRange(0, 4)) == "RIFF" &&
                String(header.copyOfRange(8, 12)) == "WEBP"
            "gif" -> header.size >= 6 &&
                (String(header.copyOfRange(0, 6)) == "GIF87a" || String(header.copyOfRange(0, 6)) == "GIF89a")
            else -> false
        }
    }

    private fun deleteImage(imagePath: String) {
        try {
            File(imagePath).delete()
        } catch (_: Exception) {
        }
    }
}
