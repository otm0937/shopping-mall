package gdfs.shopping.backend.domain

import gdfs.shopping.backend.domain.product.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductTest {

    @Test
    fun `creates product with required catalog fields`() {
        // Verifies a product stores the fields that are returned through list and detail APIs.
        val product = Product(
            name = "Keyboard",
            price = 50_000,
            description = "Mechanical keyboard",
            imagePath = "/uploads/products/keyboard.png"
        )

        assertEquals(null, product.id)
        assertEquals("Keyboard", product.name)
        assertEquals(50_000, product.price)
        assertEquals("Mechanical keyboard", product.description)
        assertEquals("/uploads/products/keyboard.png", product.imagePath)
        assertNotNull(product.createdAt)
        assertNotNull(product.updatedAt)
    }

    @Test
    fun `creates product without an image path`() {
        // Verifies image upload is optional and null image paths are valid at entity level.
        val product = Product(
            name = "Mouse",
            price = 20_000,
            description = "Wireless mouse"
        )

        assertNull(product.imagePath)
    }

    @Test
    fun `updates mutable product fields`() {
        // Verifies admin update flows can mutate name, price, description, and image path on an existing product.
        val product = Product(
            id = 1L,
            name = "Old Name",
            price = 10_000,
            description = "Old description",
            imagePath = "old.png"
        )

        product.name = "New Name"
        product.price = 15_000
        product.description = "New description"
        product.imagePath = "new.png"

        assertEquals("New Name", product.name)
        assertEquals(15_000, product.price)
        assertEquals("New description", product.description)
        assertEquals("new.png", product.imagePath)
    }

    @Test
    fun `accepts zero price for free products`() {
        // Verifies the domain model can represent zero-priced products allowed by the DTO validation rule.
        val product = Product(
            name = "Free Sticker",
            price = 0,
            description = "Promotional item"
        )

        assertEquals(0, product.price)
    }

    @Test
    fun `keeps negative price visible for validation layers to reject`() {
        // Verifies the entity itself does not hide invalid prices; controller and service tests cover rejection paths.
        val product = Product(
            name = "Invalid Product",
            price = -1,
            description = "Invalid price sample"
        )

        assertTrue(product.price < 0)
    }
}
