package ru.apokali4.aptechnayasmena

import org.junit.Assert.assertTrue
import org.junit.Test

class DrugCatalogTest {
    @Test fun everyMonographHasAllRequiredSectionsAndSource() {
        assertTrue(validateDrugCatalog(drugCatalog).isEmpty())
    }

    @Test fun catalogIdsAreUnique() {
        assertTrue(drugCatalog.map { it.id }.toSet().size == drugCatalog.size)
    }
}