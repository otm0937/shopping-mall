package gdfs.shopping.backend.service

import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import gdfs.shopping.backend.dto.product.ProductCreateRequest
import gdfs.shopping.backend.dto.product.ProductUpdateRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @TempDir
    private lateinit var uploadDir: Path

    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productService = ProductService(productRepository, uploadDir.toString())
    }

    @Test
    fun `getAllProducts maps products to list responses with public image urls`() {
        // Verifies catalog list responses expose product fields and convert stored image paths to public URLs.
        Mockito.`when`(productRepository.findAll()).thenReturn(
            listOf(
                product(id = 1L, name = "Keyboard", price = 50_000, imagePath = "/tmp/uploads/keyboard.png"),
                product(id = 2L, name = "Mouse", price = 20_000, imagePath = null)
            )
        )

        val responses = productService.getAllProducts()

        assertEquals(2, responses.size)
        assertEquals("Keyboard", responses[0].name)
        assertEquals("/uploads/products/keyboard.png", responses[0].imageUrl)
        assertEquals(null, responses[1].imageUrl)
    }

    @Test
    fun `getProduct returns detail response`() {
        // Verifies product detail lookup returns description in addition to list fields.
        Mockito.`when`(productRepository.findById(1L)).thenReturn(
            Optional.of(product(id = 1L, name = "Monitor", price = 300_000, description = "4K monitor"))
        )

        val response = productService.getProduct(1L)

        assertEquals(1L, response.id)
        assertEquals("Monitor", response.name)
        assertEquals(300_000, response.price)
        assertEquals("4K monitor", response.description)
    }

    @Test
    fun `getProduct rejects missing product`() {
        // Verifies missing product IDs produce the service error used by controllers.
        Mockito.`when`(productRepository.findById(404L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.getProduct(404L)
        }

        assertEquals("상품을 찾을 수 없습니다: 404", exception.message)
    }

    @Test
    fun `createProduct saves product without image`() {
        // Verifies admin product creation works without an optional image upload.
        Mockito.doAnswer { invocation ->
            val product = invocation.getArgument<Product>(0)
            product.copy(id = 1L)
        }.`when`(productRepository).save(anyValue())

        val response = productService.createProduct(
            ProductCreateRequest(name = "Desk", price = 120_000, description = "Standing desk"),
            image = null
        )

        val productCaptor = ArgumentCaptor.forClass(Product::class.java)
        Mockito.verify(productRepository).save(productCaptor.capture())
        assertEquals("Desk", productCaptor.value.name)
        assertEquals(null, productCaptor.value.imagePath)
        assertEquals(1L, response.id)
        assertEquals("상품이 생성되었습니다", response.message)
        assertEquals(null, response.imageUrl)
    }

    @Test
    fun `createProduct stores valid png image and returns public image url`() {
        // Verifies valid image uploads are signature-checked, stored under the upload directory, and exposed as public URLs.
        Mockito.doAnswer { invocation ->
            val product = invocation.getArgument<Product>(0)
            product.copy(id = 2L)
        }.`when`(productRepository).save(anyValue())
        val image = MockMultipartFile("image", "product.png", "image/png", pngBytes())

        val response = productService.createProduct(
            ProductCreateRequest(name = "Camera", price = 450_000, description = "Mirrorless camera"),
            image = image
        )

        assertNotNull(response.imageUrl)
        assertTrue(response.imageUrl.endsWith(".png"))
        assertTrue(Files.exists(uploadDir.resolve(response.imageUrl.substringAfterLast("/"))))
    }

    @Test
    fun `createProduct rejects unsupported image content type`() {
        // Verifies non-image uploads are rejected before a product is saved.
        val image = MockMultipartFile("image", "notes.txt", "text/plain", "not an image".toByteArray())

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.createProduct(ProductCreateRequest("Book", 15_000, "Book"), image)
        }

        assertEquals("지원하지 않는 이미지 형식입니다. JPG, PNG, WebP, GIF 파일만 업로드할 수 있습니다.", exception.message)
        Mockito.verify(productRepository, Mockito.never()).save(anyValue())
    }

    @Test
    fun `createProduct rejects empty image`() {
        // Verifies empty image files fail fast and do not create a product.
        val image = MockMultipartFile("image", "empty.png", "image/png", ByteArray(0))

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.createProduct(ProductCreateRequest("Book", 15_000, "Book"), image)
        }

        assertEquals("빈 이미지 파일은 업로드할 수 없습니다.", exception.message)
        Mockito.verify(productRepository, Mockito.never()).save(anyValue())
    }

    @Test
    fun `createProduct rejects image when signature does not match content type`() {
        // Verifies a spoofed content type is rejected by file signature validation.
        val image = MockMultipartFile("image", "fake.png", "image/png", "not a png".toByteArray())

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.createProduct(ProductCreateRequest("Book", 15_000, "Book"), image)
        }

        assertEquals("이미지 파일의 내용이 형식과 일치하지 않습니다.", exception.message)
        Mockito.verify(productRepository, Mockito.never()).save(anyValue())
    }

    @Test
    fun `updateProduct changes fields and keeps old image when no new image is uploaded`() {
        // Verifies updates without a replacement image preserve the existing image path.
        val existing = product(id = 1L, name = "Old", price = 10_000, imagePath = "/tmp/old.png")
        Mockito.`when`(productRepository.findById(1L)).thenReturn(Optional.of(existing))
        Mockito.`when`(productRepository.save(existing)).thenReturn(existing)

        val response = productService.updateProduct(
            1L,
            ProductUpdateRequest(name = "New", price = 12_000, description = "New description"),
            image = null
        )

        assertEquals("New", existing.name)
        assertEquals(12_000, existing.price)
        assertEquals("New description", existing.description)
        assertEquals("/tmp/old.png", existing.imagePath)
        assertEquals("/uploads/products/old.png", response.imageUrl)
        assertEquals("상품이 수정되었습니다", response.message)
    }

    @Test
    fun `updateProduct replaces image and deletes old image`() {
        // Verifies uploading a replacement image updates the product path and removes the old file from storage.
        val oldImagePath = uploadDir.resolve("old.png")
        Files.write(oldImagePath, pngBytes())
        val existing = product(id = 1L, name = "Old", price = 10_000, imagePath = oldImagePath.toString())
        Mockito.`when`(productRepository.findById(1L)).thenReturn(Optional.of(existing))
        Mockito.`when`(productRepository.save(existing)).thenReturn(existing)

        val response = productService.updateProduct(
            1L,
            ProductUpdateRequest(name = "New", price = 12_000, description = "New description"),
            image = MockMultipartFile("image", "new.png", "image/png", pngBytes())
        )

        assertFalse(Files.exists(oldImagePath))
        assertNotNull(existing.imagePath)
        assertTrue(existing.imagePath!!.endsWith(".png"))
        assertEquals("/uploads/products/${existing.imagePath!!.substringAfterLast("/")}", response.imageUrl)
    }

    @Test
    fun `updateProduct rejects missing product`() {
        // Verifies updates for missing IDs fail before attempting image or save work.
        Mockito.`when`(productRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.updateProduct(999L, ProductUpdateRequest("Missing", 1_000, "Missing"), null)
        }

        assertEquals("상품을 찾을 수 없습니다: 999", exception.message)
    }

    @Test
    fun `deleteProduct deletes product and its image file`() {
        // Verifies admin deletion removes both the database entity and the stored image file.
        val imagePath = uploadDir.resolve("delete.png")
        Files.write(imagePath, pngBytes())
        val existing = product(id = 1L, name = "Delete", price = 5_000, imagePath = imagePath.toString())
        Mockito.`when`(productRepository.findById(1L)).thenReturn(Optional.of(existing))

        productService.deleteProduct(1L)

        assertFalse(Files.exists(imagePath))
        Mockito.verify(productRepository).delete(existing)
    }

    @Test
    fun `deleteProduct rejects missing product`() {
        // Verifies deleting a missing product returns the same not-found service error as lookup and update.
        Mockito.`when`(productRepository.findById(404L)).thenReturn(Optional.empty())

        val exception = assertFailsWith<IllegalArgumentException> {
            productService.deleteProduct(404L)
        }

        assertEquals("상품을 찾을 수 없습니다: 404", exception.message)
    }

    private fun product(
        id: Long,
        name: String,
        price: Int,
        description: String = "$name description",
        imagePath: String? = null
    ) = Product(
        id = id,
        name = name,
        price = price,
        description = description,
        imagePath = imagePath
    )

    private fun pngBytes() = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyValue(): T {
        Mockito.any<T>()
        return null as T
    }
}
