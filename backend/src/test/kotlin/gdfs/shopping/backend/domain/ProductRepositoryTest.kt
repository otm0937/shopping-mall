package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.product.Product
import gdfs.shopping.backend.domain.product.ProductRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest @Autowired constructor(
    private val productRepository: ProductRepository
) {

    @Test
    fun `saves and finds product by id`() {
        // Verifies product CRUD persistence assigns an ID and stores catalog fields.
        val saved = productRepository.save(product(name = "Laptop", price = 1_500_000))

        val found = productRepository.findById(saved.id!!)

        assertTrue(found.isPresent)
        assertEquals("Laptop", found.get().name)
        assertEquals(1_500_000, found.get().price)
    }

    @Test
    fun `findAll returns all saved products`() {
        // Verifies product list queries return every product available to the public catalog endpoint.
        productRepository.save(product(name = "Product A", price = 1_000))
        productRepository.save(product(name = "Product B", price = 2_000))

        val products = productRepository.findAll()

        assertEquals(2, products.size)
        assertEquals(listOf("Product A", "Product B"), products.map { it.name })
    }

    @Test
    fun `updates mutable product fields`() {
        // Verifies admin updates are persisted through JPA dirty checking and save.
        val saved = productRepository.save(product(name = "Old Product", price = 5_000, imagePath = "old.png"))

        saved.name = "Updated Product"
        saved.price = 7_000
        saved.description = "Updated description"
        saved.imagePath = "updated.png"
        productRepository.saveAndFlush(saved)

        val updated = productRepository.findById(saved.id!!).get()
        assertEquals("Updated Product", updated.name)
        assertEquals(7_000, updated.price)
        assertEquals("Updated description", updated.description)
        assertEquals("updated.png", updated.imagePath)
    }

    @Test
    fun `deletes product`() {
        // Verifies product deletion removes the record used by admin delete endpoints.
        val saved = productRepository.save(product(name = "Delete Me", price = 3_000))

        productRepository.delete(saved)
        productRepository.flush()

        assertFalse(productRepository.findById(saved.id!!).isPresent)
    }

    @Test
    fun `persists zero priced product`() {
        // Verifies the repository supports DTO-validated zero price values.
        val saved = productRepository.save(product(name = "Free Gift", price = 0))

        assertEquals(0, productRepository.findById(saved.id!!).get().price)
    }

    private fun product(
        name: String,
        price: Int,
        imagePath: String? = null
    ) = Product(
        name = name,
        price = price,
        description = "$name description",
        imagePath = imagePath
    )
}
