package com.example.promisclamping.models

import com.google.gson.annotations.SerializedName

data class ClampingRequestForm(
    val id: String?,
    val noKenderaan: String?,
    val jenisKenderaan: String?, // or jenisId if your API expects an ID
    val blok: String?,
    val tempat: String?,
    val lokasi: String?,
    val status: String?,
    val catatanBatal: String?,
    val dirClamp1: String?,
    val dirClamp2: String?,
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
    val lokasi: String?,
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

// Represents a single kompaun in a list
data class KompaunItem(
    val id: String?,
    val noKompaun: String?,
    val noKenderaan: String?,
    val status: String?,
    val tarikhKompaunStr: String?,
    val masaKompaunStr: String?,
    val idPemilik: String?,
    val namaPemilik: String?,
    val jenisKenderaan: String?,
    val tempat: String?,
    val lokasi: String?,
    val kadarKompaun: String?,
    val catatanBatal: String?,
    val dirClamp2: String?,
)

data class KompaunListResponse(
    @SerializedName("data")
    val data: List<KompaunItem> = emptyList(),

    @SerializedName("pageNo")
    val pageNo: Int? = null,

    @SerializedName("totalItems")
    val totalItems: Int? = null,

    @SerializedName("totalPages")
    val totalPages: Int? = null,

    // You can adjust the type if you know what summaryData is
    @SerializedName("summaryData")
    val summaryData: Any? = null
)

