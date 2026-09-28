package com.abyxcz.starpoints.core.catalog

private const val HEADER_BYTES = 8
private const val RECORD_BYTES = 20
private const val BITS_PER_BYTE = 8
private const val MAGIC_BYTES = 4
private const val INT32_BYTES = 4
private const val COUNT_OFFSET = 4
private const val RA_OFFSET = 4
private const val DEC_OFFSET = 8
private const val MAG_OFFSET = 12
private const val BV_OFFSET = 16
private const val BYTE_MASK = 0xFF

private val MAGIC =
    byteArrayOf('S'.code.toByte(), 'P'.code.toByte(), 'C'.code.toByte(), '1'.code.toByte())

/**
 * Reads a star catalog from its binary form (little-endian).
 *
 * Layout: 4 magic bytes `S`,`P`,`C`,`1`; an Int32 record count; then per record an Int32 id,
 * Float32 RA, Float32 Dec, Float32 magnitude and Float32 B−V (20 bytes each).
 *
 * @param bytes the catalog bytes.
 * @return the decoded records.
 * @throws IllegalArgumentException on a wrong magic or a length that disagrees with the count.
 */
fun readCatalog(bytes: ByteArray): List<StarRecord> {
    require(bytes.size >= HEADER_BYTES) { "catalog too short for header" }
    require(magicMatches(bytes)) { "wrong catalog magic" }
    val count = readInt32(bytes, COUNT_OFFSET)
    val expectedSize = HEADER_BYTES + count * RECORD_BYTES
    require(bytes.size == expectedSize) { "catalog length disagrees with record count" }
    return readRecords(bytes, count)
}

private fun magicMatches(bytes: ByteArray): Boolean {
    for (i in 0 until MAGIC_BYTES) {
        if (bytes[i] != MAGIC[i]) return false
    }
    return true
}

private fun readRecords(bytes: ByteArray, count: Int): List<StarRecord> {
    val records = ArrayList<StarRecord>(count)
    var offset = HEADER_BYTES
    for (i in 0 until count) {
        val id = readInt32(bytes, offset)
        val ra = readFloat32(bytes, offset + RA_OFFSET)
        val dec = readFloat32(bytes, offset + DEC_OFFSET)
        val mag = readFloat32(bytes, offset + MAG_OFFSET)
        val bv = readFloat32(bytes, offset + BV_OFFSET)
        records.add(StarRecord(id, ra, dec, mag, bv))
        offset += RECORD_BYTES
    }
    return records
}

private fun readInt32(bytes: ByteArray, offset: Int): Int {
    var value = 0
    for (i in 0 until INT32_BYTES) {
        value = value or ((bytes[offset + i].toInt() and BYTE_MASK) shl (BITS_PER_BYTE * i))
    }
    return value
}

private fun readFloat32(bytes: ByteArray, offset: Int): Float {
    return Float.fromBits(readInt32(bytes, offset).toLong().toInt())
}
