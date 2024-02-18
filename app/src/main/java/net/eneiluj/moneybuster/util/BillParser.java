package net.eneiluj.moneybuster.util;

import android.net.Uri;

import net.eneiluj.moneybuster.model.parsed.AustrianBillQrCode;
import net.eneiluj.moneybuster.model.parsed.CroatianBillQrCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillParser {
    private static final SimpleDateFormat austrianQrCodeDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private static final DateTimeFormatter croatianQrCodeDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    /**
     * Input structure:
     * Separator: _
     * Index: Meaning
     * 1: Algorithm
     * 2: CashDeskId
     * 3: CheckNumber
     * 4: LocalDateTime
     * 5: Amount20%Tax
     * 6: Amount19%Tax
     * 7: Amount13%Tax
     * 8: Amount10%Tax
     * 9: Amount0%Tax
     * 10-13: aditionalMetadata
     * <p>
     * Example of the input:
     * _R1-AT1_rk-01_ft102DF#64551_2022-03-05T11:29:34_0,00_23,25_0,00_0,00_0,00_nxsJ06w=_6f3deaee_2FYzVcRi2NU=_ueezgtX24lRBTBwCpXbHSt7O8I3J5HO9pwYvPUd7tG6o1kPIKUAXCqvs+81DPhMRSqTke8qeVoKw/YNBrGrI9A==
     *
     * @param scannedBill
     * @return
     */
    public static AustrianBillQrCode parseAustrianBillFromQrCode(String scannedBill) throws ParseException {
        String[] splitBill = scannedBill.split("_");

        if (splitBill.length < 10) {
            throw new ParseException("Could not parse bill to Austrian format!", 0);
        }

        Date date = austrianQrCodeDateFormat.parse(splitBill[4]);
        double totalAmount = 0;
        for (int i = 1; i <= 5; i++) {
            totalAmount += SupportUtil.commaNumberFormat.parse(splitBill[4 + i]).doubleValue();
        }
        // some amounts may be negative that's why we have to round here
        return new AustrianBillQrCode(splitBill[2], date, Math.round(totalAmount * 100.0) / 100.0);
    }

    /**
     * Example input:
     * https://porezna.gov.hr/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1&datv=20230615_1045&izn=5,00
     *
     * See https://gitlab.com/eneiluj/moneybuster/-/issues/129
     */
    public static CroatianBillQrCode parseCroatianBillFromQrCode(String scannedBill) throws ParseException {
        Uri uri = Uri.parse(scannedBill);

        // Be defensive, and only allow the known host
        if (uri.getHost() == null || !uri.getHost().equals("porezna.gov.hr")) {
            throw new ParseException("Does not look like a Croatian QR code", 0);
        }

        List<String> dates = uri.getQueryParameters("datv");
        List<String> amounts = uri.getQueryParameters("izn");

        LocalDateTime date = null;
        if (!dates.isEmpty()) {
            date = LocalDateTime.parse(dates.get(0), croatianQrCodeDateFormat);
        }

        Double amount = null;
        if (!amounts.isEmpty()) {
            try {
                amount = SupportUtil.commaNumberFormat.parse(amounts.get(0)).doubleValue();
            } catch (NullPointerException e) {
                // failed to parse as double
            }
        }

        if (date == null && amount == null) {
            throw new ParseException("Could not parse bill to Croatian format!", 0);
        }
        return new CroatianBillQrCode(
                date, // can be null
                amount == null ? 0.0 : amount
        );
    }
}
