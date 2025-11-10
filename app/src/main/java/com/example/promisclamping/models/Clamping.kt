package com.example.promisclamping.models

data class ClampingRequestForm(
    val noKenderaan: String,
    val jenisKenderaan: String, // or jenisId if your API expects an ID
    val blok: String?,
    val tempat: String?,
    val dirClamp1: String?
)

// ClampingResponseForm → exactly what your server returns
data class ClampingResponseForm(
    val id: String,
    val noKompaun: String?,
    val tarikhKompaun: String?,
    val tarikhKompaunStr: String?,
    val masaKompaunStr: String?,
    val noKenderaan: String?,
    val idPemilik: String?,
    val namaPemilik: String?,
    val noCukai: String?,
    val jenisKenderaan: String?,
    val warna: String?,
    val noPetak: String?,
    val tempat: String?,
    val kadarKompaun: String?,
    val idPegawai: String?,
    val namaPegawai: String?,
    val kodPegawai: String?,
    val idPegawaiBatal: String?,
    val namaPegawaiBatal: String?,
    val catatanBatal: String?,
    val tarikhBatal: String?,
    val status: String?,
    val idResit: String?,
    val noResit: String?,
    val tarikhResit: String?,
    val noPengenalanPembayar: String?,
    val namaPembayar: String?,
    val dirClamp1: String?,
    val dirClamp2: String?,
    val idPegawaiSelesai: String?,
    val tarikhSelesai: String?,
    val namaPegawaiSelesai: String?,

    val idDaftar: String?,
    val daftarOleh: String?,
    val tarikhDaftar: String?,
    val idKemaskini: String?,
    val kemaskiniOleh: String?,
    val tarikhKemaskini: String?,
)