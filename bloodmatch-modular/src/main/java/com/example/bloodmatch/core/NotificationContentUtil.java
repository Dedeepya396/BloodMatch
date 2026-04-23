package com.example.bloodmatch.core;

/**
 * Utility class to provide beautiful HTML-formatted notification content.
 */
public class NotificationContentUtil {

    /**
     * Returns a rich HTML-formatted string containing donor guidelines and ineligibility criteria.
     * Use this to append to email body for a professional look.
     */
    public static String getDonorGuidelinesHtml() {
        return "<div style='margin-top:30px; border-top:2px solid #eee; padding-top:20px; font-family: sans-serif;'>" +
               "  <h3 style='color:#c0392b; border-bottom:1px solid #c0392b; padding-bottom:5px; margin-bottom:15px;'>🩸 Donor Guidelines & Information</h3>" +
               
               "  <div style='margin-bottom:20px;'>" +
               "    <strong style='color:#2c3e50; display:block; margin-bottom:8px;'>Eligibility Criteria:</strong>" +
               "    <ul style='margin:0; padding-left:20px; color:#555;'>" +
               "      <li><strong>Age:</strong> 18-60 years</li>" +
               "      <li><strong>Weight:</strong> More than 45 kg</li>" +
               "      <li><strong>Hemoglobin:</strong> Men >12 g/dL | Women >12.5 g/dL</li>" +
               "      <li><strong>Pulse:</strong> 50-100/min (regular)</li>" +
               "      <li><strong>Blood Pressure:</strong> Systolic 100-180 | Diastolic 50-100</li>" +
               "      <li><strong>Health:</strong> Must be in good physical and mental health</li>" +
               "    </ul>" +
               "  </div>" +

               "  <div style='margin-bottom:20px;'>" +
               "    <strong style='color:#2c3e50; display:block; margin-bottom:8px;'>Preparation Before Donation:</strong>" +
               "    <ul style='margin:0; padding-left:20px; color:#555;'>" +
               "      <li>Get adequate rest and sleep the night before.</li>" +
               "      <li>Have a light meal/breakfast at least 2 hours before donating.</li>" +
               "      <li>Ensure you are mentally prepared and comfortable.</li>" +
               "    </ul>" +
               "  </div>" +

               "  <div style='margin-bottom:20px; border:1px solid #f8d7da; background-color:#fff5f5; padding:15px; border-radius:8px;'>" +
               "    <strong style='color:#721c24; display:block; margin-bottom:8px;'>Ineligibility Criteria:</strong>" +
               "    <p style='margin:0 0 10px 0; font-size:14px; color:#856404;'><em>Do not donate if you fit any of the following timeframes:</em></p>" +
               "    <ul style='margin:0; padding-left:20px; color:#721c24; font-size:14px;'>" +
               "      <li><strong>Past 1 Year:</strong> Surgery, Typhoid, Dog bite, Unexplained weight loss.</li>" +
               "      <li><strong>Past 6 Months:</strong> Tattoo, Piercing, Dental Extraction, Malaria, Vaccination.</li>" +
               "      <li><strong>Past 21 Days:</strong> Conjunctivitis.</li>" +
               "      <li><strong>Past 24 Hours:</strong> Alcohol consumption.</li>" +
               "    </ul>" +
               "  </div>" +

               "  <div style='margin-bottom:10px; padding:15px; background-color:#f0f7ff; border-radius:8px; border-left:4px solid #3498db;'>" +
               "    <strong style='color:#2980b9; display:block; margin-bottom:5px;'>For Female Donors:</strong>" +
               "    <p style='margin:0; font-size:14px; color:#34495e;'>" +
               "      Donation is not permitted during pregnancy, lactation, within one year after delivery, or during/shortly after menstrual periods (up to 7 days)." +
               "    </p>" +
               "  </div>" +
               "</div>";
    }
}
