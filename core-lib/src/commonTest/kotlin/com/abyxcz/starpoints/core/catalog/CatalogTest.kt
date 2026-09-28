package com.abyxcz.starpoints.core.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val HEADER_BYTES = 8
private const val RECORD_BYTES = 20
private const val BITS_PER_BYTE = 8
private const val INT32_BYTES = 4
private const val RA_OFFSET = 4
private const val DEC_OFFSET = 8
private const val MAG_OFFSET = 12
private const val BV_OFFSET = 16
private const val COUNT_OFFSET = 4
private const val BYTE_MASK = 0xFF

class CatalogTest {
    @Test
    fun roundTripsTwoRecords() {
        val sirius = StarRecord(32349, 101.28716f, -16.71612f, -1.46f, 0.00f)
        val vega = StarRecord(91262, 279.23473f, 38.78369f, 0.03f, 0.00f)
        val bytes = encode(listOf(sirius, vega))
        val decoded = readCatalog(bytes)
        assertEquals(2, decoded.size)
        assertEquals(32349, decoded[0].id)
        assertEquals(101.28716f, decoded[0].rightAscensionDegrees, 1e-4f)
        assertEquals(-16.71612f, decoded[0].declinationDegrees, 1e-4f)
        assertEquals(-1.46f, decoded[0].magnitude, 1e-4f)
        assertEquals(0.00f, decoded[0].colorIndex, 1e-4f)
        assertEquals(91262, decoded[1].id)
        assertEquals(279.23473f, decoded[1].rightAscensionDegrees, 1e-4f)
        assertEquals(38.78369f, decoded[1].declinationDegrees, 1e-4f)
        assertEquals(0.03f, decoded[1].magnitude, 1e-4f)
        assertEquals(0.00f, decoded[1].colorIndex, 1e-4f)
    }

    @Test
    fun emptyCatalog() {
        val bytes = encode(emptyList())
        val decoded = readCatalog(bytes)
        assertEquals(0, decoded.size)
    }

    @Test
    fun wrongMagicThrows() {
        val bytes = encode(listOf(StarRecord(1, 0.0f, 0.0f, 0.0f, 0.0f)))
        bytes[0] = 'X'.code.toByte()
        assertFailsWith<IllegalArgumentException> { readCatalog(bytes) }
    }

    @Test
    fun truncatedRecordThrows() {
        val bytes = encode(listOf(StarRecord(1, 0.0f, 0.0f, 0.0f, 0.0f)))
        val truncated = bytes.copyOfRange(0, bytes.size - 1)
        assertFailsWith<IllegalArgumentException> { readCatalog(truncated) }
    }

    private fun encode(records: List<StarRecord>): ByteArray {
        val out = ByteArray(HEADER_BYTES + records.size * RECORD_BYTES)
        out[0] = 'S'.code.toByte()
        out[1] = 'P'.code.toByte()
        out[2] = 'C'.code.toByte()
        out[3] = '1'.code.toByte()
        writeInt32(out, COUNT_OFFSET, records.size)
        var offset = HEADER_BYTES
        for (r in records) {
            writeInt32(out, offset, r.id)
            writeFloat32(out, offset + RA_OFFSET, r.rightAscensionDegrees)
            writeFloat32(out, offset + DEC_OFFSET, r.declinationDegrees)
            writeFloat32(out, offset + MAG_OFFSET, r.magnitude)
            writeFloat32(out, offset + BV_OFFSET, r.colorIndex)
            offset += RECORD_BYTES
        }
        return out
    }

    private fun writeInt32(out: ByteArray, offset: Int, value: Int) {
        for (i in 0 until INT32_BYTES) {
            out[offset + i] = ((value shr (BITS_PER_BYTE * i)) and BYTE_MASK).toByte()
        }
    }

    private fun writeFloat32(out: ByteArray, offset: Int, value: Float) {
        writeInt32(out, offset, value.toRawBits())
    }
}
