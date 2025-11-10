package com.example.promisclamping.print

fun buildNotisCajReceipt(
    noSiri: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String,
    officerId: String = ""
): String {

    val W = 576
    val L = 20
    val fullW = W - 40
    var y = 30

    fun center(h: Int, font: String = "1", xMul: Int = 1, yMul: Int = 1, text: String) =
        "BLOCK $L,$y,$fullW,$h,\"$font\",0,$xMul,$yMul,1,2,\"$text\""
            .also { y += h + 4 }

    fun left(h: Int, font: String = "1", text: String) =
        "BLOCK $L,$y,$fullW,$h,\"$font\",0,1,1,0,2,\"$text\""
            .also { y += h + 4 }

    // add these two overloads right below the originals
    fun center(h: Int, text: String) = center(h, "1", 1, 1, text)
    fun left(h: Int, text: String) = left(h, "1", text)

    fun row(k: String, v: String): String {
        val keyW = 220
        val work = """
        BLOCK ${L + 8},$y,${keyW - 16},26,"1",0,1,1,0,2,"$k"
        BLOCK ${L + keyW + 8},$y,${fullW - keyW - 16},26,"1",0,1,1,0,2,"$v"
        """.trimIndent()
        y += 30
        return work
    }

    return buildString {
        appendLine("SIZE 72 mm,150 mm")
        appendLine("GAP 0,0")
        appendLine("DIRECTION 0")
        appendLine("DENSITY 14")
        appendLine("SPEED 4")
        appendLine("CLS")

        // Jata logo from printer flash
//        appendLine("PUTBMP 96,5,\"JATA.BMP\"")
        y = 115

        appendLine(center(26, "JABATAN PERDANA MENTERI"))
        appendLine(center(26, "BAHAGIAN PENGURUSAN HARTANAH"))
        appendLine(center(22, "NO SIRI : $noSiri"))


        appendLine(
            left(
                48, text =
                    "Tuan/Puan telah meletak kenderaan di kawasan tidak dibenarkan. " +
                            "Tayar kenderaan telah diapit dan caj dikenakan."
            )
        )

        val tableTop = y
        appendLine("BOX $L,$tableTop,${W - 20},${tableTop + 180},2")

        appendLine(row("TARIKH", tarikh))
        appendLine("LINE $L,$y,${W - 20},$y,2")

        appendLine(row("MASA", masa))
        appendLine("LINE $L,$y,${W - 20},$y,2")

        appendLine(row("NO. KENDERAAN", noKenderaan))
        appendLine("LINE $L,$y,${W - 20},$y,2")

        appendLine(row("KADAR CAJ", "$kadarCaj $jenisKenderaan"))
        appendLine("LINE $L,$y,${W - 20},$y,2")

        appendLine(row("LOKASI", lokasi))
        appendLine("LINE $L,$y,${W - 20},$y,2")

        appendLine(row("PEGAWAI", pegawai))

        y += 12
        appendLine(left(22,"Sila jelaskan caj untuk membuka apitan dan kunci di alamat dan waktu berikut:"))
        y += 6

// Header above box
        appendLine(left(24,"Kaedah Pembayaran          Tempat Pembayaran          Waktu Pembayaran"))
        y += 4

// Table start
        val payTop = y
        val col1 = L + 4
        val col2 = L + 210
        val col3 = L + 405
        val rowH = 30

// Outer border
        appendLine("BOX $L,$payTop,${W-20},${payTop + rowH*3},2")

        fun trow(a:String,b:String,c:String) {
            appendLine("""BLOCK $col1,$y,${col2-col1-8},$rowH,"1",0,1,1,0,2,"$a"""")
            appendLine("""BLOCK ${col2+4},$y,${col3-col2-8},$rowH,"1",0,1,1,0,2,"$b"""")
            appendLine("""BLOCK ${col3+4},$y,${(W-20)-col3-8},$rowH,"1",0,1,1,0,2,"$c"""")
            y += rowH
            appendLine("LINE $L,$y,${W-20},$y,2")
        }

// Vertical dividers
        appendLine("LINE $col2,$payTop,$col2,${payTop+rowH*3},2")
        appendLine("LINE $col3,$payTop,$col3,${payTop+rowH*3},2")

// Rows
        trow("Kaedah", "Kaunter Hasil BPH\nBlok F6 JPM", "Isnin–Khamis\n9.00 pg – 4.00 ptg")
        trow("Tunai", "Pusat Pentadbiran\nPutrajaya", "Jumaat\n9–12 tgh & 3–4 ptg")
        trow("Dalam Talian", "https://promis.bph.gov.my", "24 jam")

        y += 12


        // Signature + officer ID
        y += 10
        appendLine(center(20, pegawai))
        appendLine(center(20, "Pegawai BPH"))
        if(officerId.isNotEmpty())
            appendLine(center(20, "ID: $officerId"))

        y += 10
        appendLine("QRCODE 160,$y,L,7,A,0,\"$savedId\"")

        appendLine("PRINT 1,1")
    }
}

fun testPrintHello(): String = """
    SIZE 72 mm,30 mm
    GAP 0,0
    CLS
    TEXT 20,20,"1",0,1,1,"HELLO TEST"
    PRINT 1,1
""".trimIndent()

fun testPrintFile(): String = """
    SIZE 72 mm,100 mm
    GAP 0,0
    DIRECTION 0
    SPEED 4
    DENSITY 11
    CLS
    FILES
    PRINT 1,1
""".trimIndent()

fun testPrintA(): String = """
    SIZE 72 mm,152 mm
    GAP 0,0
    DIRECTION 0
    SPEED 4
    DENSITY 11
    CLS
    TEXT 20,20,"1",0,1,1,"TEST A"
    PRINT 1,1
""".trimIndent()

fun testPrintB(): String = """
    SIZE 72 mm,152 mm
    GAP 0,0
    DIRECTION 0
    SPEED 4
    DENSITY 11
    CLS    
    BLOCK 20,20,400,40,"1",0,1,1,0,2,"TEST B"
    PRINT 1,1
""".trimIndent()

fun testPrintC(): String = """
    SIZE 72 mm,152 mm
    GAP 0,0
    DIRECTION 0
    SPEED 4
    DENSITY 11
    CLS
    TEXT 20,20,"1",0,2,2,"NOTIS CAJ"
    PRINT 1,1
""".trimIndent()

fun buildTsplNotisCaj(
    noKompaun: String,
    tarikh: String,
    masa: String,
    noKenderaan: String,
    kadarCaj: String,
    jenisKenderaan: String,
    lokasi: String,
    pegawai: String,
    savedId: String,
    labelHeightMm: Int = 130
): String {

    val W = 576
    val L = 20
    val fullW = W - 40
    var y = 10

    fun BLOCKC(h: Int, f: String = "1", xm: Int = 1, ym: Int = 1, text: String) =
        "BLOCK $L,$y,$fullW,$h,\"$f\",0,$xm,$ym,1,2,\"$text\"".also { y += h + 4 }

    fun BLOCKL(h: Int, f: String = "1", xm: Int = 1, ym: Int = 1, text: String) =
        "BLOCK $L,$y,$fullW,$h,\"$f\",0,$xm,$ym,0,2,\"$text\"".also { y += h + 4 }

    fun ROW(label: String, value: String): String {
        val k = 220
        return """
            BLOCK ${L + 8},$y,${k - 16},22,"1",0,1,1,0,2,"$label"
            BLOCK ${L + k + 8},$y,${fullW - k - 16},22,"1",0,1,1,0,2,"$value"
        """.trimIndent().also { y += 30 }
    }

    return buildString {
        appendLine("SIZE 72 mm,${labelHeightMm} mm")
        appendLine("GAP 0,0")
        appendLine("DIRECTION 0")
        appendLine("SPEED 4")
        appendLine("DENSITY 11")
        appendLine("CLS")

        appendLine(BLOCKC(24, "1", 1, 1, "JABATAN PERDANA MENTERI"))
        appendLine(BLOCKC(24, "1", 1, 1, "BAHAGIAN PENGURUSAN HARTANAH"))
        appendLine(BLOCKC(36, "2", 2, 2, "NOTIS CAJ"))

        appendLine(BLOCKC(22, "1", 1, 1, "NO SIRI : $noKompaun"))
        y += 4

        appendLine(
            BLOCKL(
                48, "1", 1, 1,
                "Tuan/Puan telah meletak kenderaan di tempat yang tidak dibenarkan di Kompleks F. " +
                        "Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan."
            )
        )
        y += 6

        val tableTop = y
        appendLine("BOX $L,$tableTop,${W - 20},${tableTop + 180},2")

        appendLine(ROW("TARIKH", tarikh))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("MASA", masa))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("NOMBOR KENDERAAN", noKenderaan))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("KADAR CAJ", "$kadarCaj   $jenisKenderaan"))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("LOKASI", lokasi))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("DIKELUARKAN OLEH", pegawai))
        y += 10

        y += 6
        // Section title
        appendLine(
            BLOCKL(
                24,
                "1",
                1,
                1,
                "Sila jelaskan kadar caj yang dikenakan untuk membuka apitan dan kunci"
            )
        )
        y += 4

        // Payment Table
        val payTop = y
        val col1 = L + 8
        val col2 = L + 260
        val rowH = 26

        // Outer border box
        appendLine("BOX $L,$payTop,${W - 20},${payTop + (rowH * 5) + 10},2")

        fun payRow(label: String, value: String) {
            appendLine("""BLOCK $col1,$y,${col2 - col1 - 8},$rowH,"1",0,1,1,0,2,"$label"""")
            appendLine("""BLOCK $col2,$y,${(W - 20) - col2 - 8},$rowH,"1",0,1,1,0,2,"$value"""")
            y += rowH
            appendLine("LINE $L,$y,${W - 20},$y,2")
        }

        // vertical divider
        appendLine("LINE $col2,$payTop,$col2,${payTop + (rowH * 5) + 10},2")

        payRow("Kaedah", "Tunai / Online")
        payRow("Kaunter", "Blok F6 (BPH)")
        payRow("Waktu", "Isnin–Khamis 9-4 ptg")
        payRow("", "Jumaat 9-12, 3-4 ptg")
        payRow("Online", "https://promis.bph.gov.my (24 jam)")


        y += 10
        appendLine("QRCODE 160,$y,L,7,A,0,\"$savedId\"")

        appendLine("PRINT 1,1")
    }
}

private fun buildTsplNotisCajFinal(
    noKompaun: String, tarikh: String, masa: String,
    noKenderaan: String, kadarCaj: String, jenisKenderaan: String,
    lokasi: String, pegawai: String, savedId: String
): String {

    val W = 576
    val L = 20
    val fullW = W - 40
    var y = 10

    fun B(
        h: Int,
        font: String = "1",
        xm: Int = 1,
        ym: Int = 1,
        align: Int = 0,
        text: String
    ): String {
        val line = "BLOCK $L,$y,$fullW,$h,\"$font\",0,$xm,$ym,$align,2,\"$text\""
        y += h + 4
        return line
    }

    fun ROW(k: String, v: String): String {
        val keyW = 220
        val txt = """
        BLOCK ${L + 8},$y,${keyW - 16},24,"1",0,1,1,0,2,"$k"
        BLOCK ${L + keyW + 8},$y,${fullW - keyW - 16},24,"1",0,1,1,0,2,"$v"
        """.trimIndent()
        y += 28
        return txt
    }

    return buildString {
        appendLine("SIZE 72 mm,130 mm")
        appendLine("GAP 0,0")
        appendLine("DIRECTION 1")
        appendLine("SPEED 4")
        appendLine("DENSITY 12")
        appendLine("CLS")

        appendLine(B(26, "1", 1, 1, 1, "JABATAN PERDANA MENTERI"))
        appendLine(B(26, "1", 1, 1, 1, "BAHAGIAN PENGURUSAN HARTANAH"))
        appendLine(B(40, "TSS48.BF2", 2, 2, 1, "NOTIS CAJ"))
        appendLine(B(24, "1", 1, 1, 1, "NO SIRI : $noKompaun"))

        appendLine(
            B(
                46, "1", 1, 1, 0,
                "Tuan/Puan telah meletak kenderaan di Kompleks F dan tayar " +
                        "kenderaan telah diapit. Caj akan dikenakan."
            )
        )

        val tableTop = y
        appendLine("BOX $L,$tableTop,${W - 20},${tableTop + 180},2")

        appendLine(ROW("TARIKH", tarikh))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("MASA", masa))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("NO. KENDERAAN", noKenderaan))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("KADAR CAJ", "$kadarCaj $jenisKenderaan"))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("LOKASI", lokasi))
        appendLine("LINE $L,${y},${W - 20},${y},2")
        appendLine(ROW("PEGAWAI", pegawai))

        y += 12

        appendLine(B(22, "1", 1, 1, 0, "Kaedah Bayaran: Tunai / Online"))
        appendLine(B(20, "1", 1, 1, 0, "Tunai: Kaunter Hasil BPH F6"))
        appendLine(B(20, "1", 1, 1, 0, "Isnin–Khamis: 9.00–4.00 ptg"))
        appendLine(B(20, "1", 1, 1, 0, "Jumaat: 9.00–12.00 & 3.00–4.00"))
        appendLine(B(20, "1", 1, 1, 0, "Online: https://promis.bph.gov.my"))

        y += 10
        appendLine("QRCODE 160,$y,L,7,A,0,\"$savedId\"")

        appendLine("PRINT 1,1")
    }
}

fun buildTsplNotisCajV2(
    noKompaun: String,      // e.g. BPH/2025/11/0031
    tarikh: String,         // e.g. 17/09/2025
    masa: String,           // e.g. 3.00 PM
    noKenderaan: String,    // e.g. AHB5677
    kadarCaj: String,       // e.g. RM 50.00
    jenisKenderaan: String, // e.g. KERETA / MOTOSIKAL
    lokasi: String,         // e.g. BLOK F6 - P1 LALUAN KECEMASAN
    pegawai: String,        // e.g. OLEH AZURA BINTI ZAKARIA
    savedId: String,        // QR payload
    labelHeightMm: Int = 110 // make room for everything incl. QR (tweak if needed)
): String {

    // Label geometry
    val W = 576                       // 72 mm @ 203 dpi
    val L = 20                        // left margin
    val R = W - 20                    // right margin
    val fullW = R - L
    val lineThick = 2

    // Shortcuts for drawing
    fun BOX(x1: Int, y1: Int, x2: Int, y2: Int, t: Int = lineThick) = "BOX $x1,$y1,$x2,$y2,$t"
    fun LINE(x1: Int, y1: Int, x2: Int, y2: Int, t: Int = lineThick) = "LINE $x1,$y1,$x2,$y2,$t"

    // BLOCK x,y,width,height,font,rotation,xMul,yMul,alignment,spacing,text
    // alignment: 0=left, 1=center, 2=right, 3=justify
    fun BLOCK(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        font: String = "1",
        xm: Int = 1,
        ym: Int = 1,
        align: Int = 0,
        space: Int = 2,
        text: String
    ) =
        "BLOCK $x,$y,$w,$h,\"$font\",0,$xm,$ym,$align,$space,\"$text\""

    // Row helper for table
    fun row(y: Int, key: String, value: String, keyW: Int): List<String> {
        val keyBox = BLOCK(L + 8, y + 6, keyW - 16, 20, "1", 1, 1, 0, 2, key)
        val valBox = BLOCK(L + keyW + 8, y + 6, fullW - keyW - 16, 20, "1", 1, 1, 0, 2, value)
        val sep = LINE(L, y + 28, R, y + 28)
        return listOf(keyBox, valBox, sep)
    }

    val keyColW = 220     // width of left column in the table (dots)

    var y = 0
    return buildString {
        appendLine("SIZE 72 mm,${labelHeightMm} mm")
        appendLine("GAP 0,0")
        appendLine("DIRECTION 0")
        appendLine("SPEED 4")
        appendLine("DENSITY 11")
        appendLine("CLS")

        // 1–3) Centered headers with spacing
        y = 12
        appendLine(BLOCK(L, y, fullW, 22, "1", 1, 1, 1, 2, "JABATAN PERDANA MENTERI")); y += 22
        appendLine(BLOCK(L, y, fullW, 22, "1", 1, 1, 1, 2, "BAHAGIAN PENGURUSAN HARTANAH")); y += 30

        appendLine(
            BLOCK(
                L,
                y,
                fullW,
                36,
                "2",
                2,
                2,
                1,
                2,
                "NOTIS CAJ"
            )
        ); y += 42  // big centered title

        // 4) NO SIRI on its own line, small spacing below
        appendLine(BLOCK(L, y, fullW, 20, "1", 1, 1, 0, 2, "NO SIRI : $noKompaun")); y += 26

        // 5) Justified paragraph (auto-wrap to full width)
        val para =
            "Tuan/Puan telah meletak kenderaan di kawasan tidak dibenarkan di kompleks F. Oleh itu, tayar kenderaan tuan/puan telah diapit dan caj akan dikenakan."
        appendLine(BLOCK(L, y, fullW, 48, "1", 1, 1, 3, 2, para)); y += 56

        // 6 & 8) Bordered info table
        val tableTop = y
        val rows = listOf(
            "TARIKH" to tarikh,
            "MASA" to masa,
            "NOMBOR KENDERAAN" to noKenderaan,
            "KADAR CAJ" to "$kadarCaj   $jenisKenderaan",
            "LOKASI" to lokasi,
            "DIKELUARKAN OLEH" to pegawai
        )
        val rowHeight = 30
        val tableBottom = tableTop + rowHeight * rows.size + 6
        appendLine(BOX(L, tableTop, R, tableBottom))

        var ry = tableTop
        rows.forEachIndexed { i, (k, v) ->
            row(ry, k, v, keyColW).forEach { appendLine(it) }
            ry += rowHeight
        }
        y = tableBottom + 10

        // Bottom-center QR (size 7 ≈ 245 dots; center x ≈ (576-245)/2 ≈ 165)
        val qrX = 165
        val qrY = y + 6
        appendLine("QRCODE $qrX,$qrY,L,7,A,0,\"$savedId\"")

        appendLine("PRINT 1,1")
    }
}