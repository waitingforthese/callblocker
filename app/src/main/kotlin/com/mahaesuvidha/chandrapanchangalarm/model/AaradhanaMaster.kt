package com.mahaesuvidha.chandrapanchangalarm.model

/** Traditional devotional mappings used by the Aaradhana feature. */
data class AaradhanaInfo(val deity: String, val mantra: String)

object AaradhanaMaster {
    private val nakshatra = mapOf(
        "अश्विनी" to AaradhanaInfo("अश्विनीकुमार", "ॐ अश्विभ्यां नमः"),
        "भरणी" to AaradhanaInfo("यम", "ॐ यमाय नमः"),
        "कृत्तिका" to AaradhanaInfo("अग्नी", "ॐ अग्नये नमः"),
        "रोहिणी" to AaradhanaInfo("प्रजापती", "ॐ प्रजापतये नमः"),
        "मृगशीर्ष" to AaradhanaInfo("सोम", "ॐ सोमाय नमः"),
        "आर्द्रा" to AaradhanaInfo("रुद्र", "ॐ रुद्राय नमः"),
        "पुनर्वसू" to AaradhanaInfo("अदिती", "ॐ अदित्यै नमः"),
        "पुष्य" to AaradhanaInfo("बृहस्पती", "ॐ बृहस्पतये नमः"),
        "आश्लेषा" to AaradhanaInfo("नाग", "ॐ नागाय नमः"),
        "मघा" to AaradhanaInfo("पितृ", "ॐ पितृभ्यो नमः"),
        "पूर्वाफाल्गुनी" to AaradhanaInfo("भग", "ॐ भगाय नमः"),
        "उत्तराफाल्गुनी" to AaradhanaInfo("अर्यमा", "ॐ अर्यम्णे नमः"),
        "हस्त" to AaradhanaInfo("सविता", "ॐ सवित्रे नमः"),
        "चित्रा" to AaradhanaInfo("त्वष्टा", "ॐ त्वष्ट्रे नमः"),
        "स्वाती" to AaradhanaInfo("वायू", "ॐ वायवे नमः"),
        "विशाखा" to AaradhanaInfo("इंद्र-अग्नी", "ॐ इन्द्राग्निभ्यां नमः"),
        "अनुराधा" to AaradhanaInfo("मित्र", "ॐ मित्राय नमः"),
        "ज्येष्ठा" to AaradhanaInfo("इंद्र", "ॐ इन्द्राय नमः"),
        "मूळ" to AaradhanaInfo("निरृति", "ॐ निरृतये नमः"),
        "पूर्वाषाढा" to AaradhanaInfo("आपः", "ॐ अद्भ्यो नमः"),
        "उत्तराषाढा" to AaradhanaInfo("विश्वेदेव", "ॐ विश्वेभ्यो देवेभ्यो नमः"),
        "श्रवण" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः"),
        "धनिष्ठा" to AaradhanaInfo("वसू", "ॐ वसुभ्यो नमः"),
        "शतभिषा" to AaradhanaInfo("वरुण", "ॐ वरुणाय नमः"),
        "पूर्वाभाद्रपदा" to AaradhanaInfo("अजैकपाद", "ॐ अजैकपादाय नमः"),
        "उत्तराभाद्रपदा" to AaradhanaInfo("अहिर्बुध्न्य", "ॐ अहिर्बुध्न्याय नमः"),
        "रेवती" to AaradhanaInfo("पूषा", "ॐ पूष्णे नमः")
    )

    private val yoga = mapOf(
        "विष्कंभ" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः"),
        "प्रीति" to AaradhanaInfo("कामदेव", "ॐ कामाय नमः"),
        "आयुष्मान" to AaradhanaInfo("सूर्य", "ॐ सूर्याय नमः"),
        "सौभाग्य" to AaradhanaInfo("ब्रह्मा", "ॐ ब्रह्मणे नमः"),
        "शोभन" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः"),
        "अतिगंड" to AaradhanaInfo("गणेश", "ॐ गं गणपतये नमः"),
        "सुकर्मा" to AaradhanaInfo("इंद्र", "ॐ इन्द्राय नमः"),
        "धृति" to AaradhanaInfo("पृथ्वी", "ॐ पृथिव्यै नमः"),
        "शूल" to AaradhanaInfo("रुद्र", "ॐ रुद्राय नमः"),
        "गंड" to AaradhanaInfo("गणेश", "ॐ गं गणपतये नमः"),
        "वृद्धि" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः"),
        "ध्रुव" to AaradhanaInfo("ध्रुव", "ॐ ध्रुवाय नमः"),
        "व्याघात" to AaradhanaInfo("वायू", "ॐ वायवे नमः"),
        "हर्षण" to AaradhanaInfo("सूर्य", "ॐ सूर्याय नमः"),
        "वज्र" to AaradhanaInfo("इंद्र", "ॐ इन्द्राय नमः"),
        "सिद्धि" to AaradhanaInfo("गणेश", "ॐ गं गणपतये नमः"),
        "व्यतीपात" to AaradhanaInfo("रुद्र", "ॐ रुद्राय नमः"),
        "वरीयान" to AaradhanaInfo("कुबेर", "ॐ कुबेराय नमः"),
        "परिघ" to AaradhanaInfo("शनि", "ॐ शनैश्चराय नमः"),
        "शिव" to AaradhanaInfo("शिव", "ॐ नमः शिवाय"),
        "सिद्ध" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः"),
        "साध्य" to AaradhanaInfo("साध्य देव", "ॐ साध्येभ्यो नमः"),
        "शुभ" to AaradhanaInfo("लक्ष्मी", "ॐ श्रीं महालक्ष्म्यै नमः"),
        "शुक्ल" to AaradhanaInfo("चंद्र", "ॐ सोमाय नमः"),
        "ब्रह्म" to AaradhanaInfo("ब्रह्मा", "ॐ ब्रह्मणे नमः"),
        "इंद्र" to AaradhanaInfo("इंद्र", "ॐ इन्द्राय नमः"),
        "वैधृति" to AaradhanaInfo("विष्णू", "ॐ विष्णवे नमः")
    )

    private val karana = mapOf(
        "बव" to AaradhanaInfo("इंद्र", "ॐ इन्द्राय नमः"),
        "बालव" to AaradhanaInfo("ब्रह्मा", "ॐ ब्रह्मणे नमः"),
        "कौलव" to AaradhanaInfo("मित्र", "ॐ मित्राय नमः"),
        "तैतिल" to AaradhanaInfo("अर्यमा", "ॐ अर्यम्णे नमः"),
        "गर" to AaradhanaInfo("पृथ्वी", "ॐ पृथिव्यै नमः"),
        "वणिज" to AaradhanaInfo("लक्ष्मी", "ॐ श्रीं महालक्ष्म्यै नमः"),
        "विष्टि" to AaradhanaInfo("शनि", "ॐ शनैश्चराय नमः"),
        "शकुनि" to AaradhanaInfo("कालभैरव", "ॐ कालभैरवाय नमः"),
        "चतुष्पाद" to AaradhanaInfo("रुद्र", "ॐ रुद्राय नमः"),
        "नाग" to AaradhanaInfo("नाग", "ॐ नागाय नमः"),
        "किंस्तुघ्न" to AaradhanaInfo("सूर्य", "ॐ सूर्याय नमः")
    )

    private val graha = mapOf(
        "सूर्य" to AaradhanaInfo("सूर्यदेव", "ॐ सूर्याय नमः"),
        "चंद्र" to AaradhanaInfo("चंद्रदेव", "ॐ सोमाय नमः"),
        "मंगळ" to AaradhanaInfo("मंगळदेव", "ॐ अंगारकाय नमः"),
        "बुध" to AaradhanaInfo("बुधदेव", "ॐ बुधाय नमः"),
        "गुरु" to AaradhanaInfo("बृहस्पती", "ॐ बृहस्पतये नमः"),
        "शुक्र" to AaradhanaInfo("शुक्रदेव", "ॐ शुक्राय नमः"),
        "शनि" to AaradhanaInfo("शनैश्चर", "ॐ शनैश्चराय नमः"),
        "राहू" to AaradhanaInfo("राहू", "ॐ राहवे नमः"),
        "केतू" to AaradhanaInfo("केतू", "ॐ केतवे नमः")
    )

    fun forGraha(name: String): AaradhanaInfo = graha[name] ?: AaradhanaInfo("ईश्वर", "ॐ नमः शिवाय")

    fun forNakshatra(name: String): AaradhanaInfo = nakshatra[name] ?: AaradhanaInfo("ईश्वर", "ॐ नमः शिवाय")
    fun forYoga(name: String): AaradhanaInfo = yoga[name] ?: AaradhanaInfo("ईश्वर", "ॐ नमः शिवाय")
    fun forKarana(name: String): AaradhanaInfo = karana[name] ?: AaradhanaInfo("ईश्वर", "ॐ नमः शिवाय")
}
