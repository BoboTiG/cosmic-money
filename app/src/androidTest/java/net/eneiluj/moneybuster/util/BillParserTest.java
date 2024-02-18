package net.eneiluj.moneybuster.util;


import junit.framework.TestCase;

import net.eneiluj.moneybuster.model.parsed.CroatianBillQrCode;

import java.text.ParseException;
import java.time.LocalDateTime;


public class BillParserTest extends TestCase {

    public void testCroatianSimple() throws ParseException {
        String raw = "https://porezna.gov.hr/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1&datv=20230615_1045&izn=5,00";
        CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
        LocalDateTime expectedDate = LocalDateTime.of(2023, 6, 15, 10, 45);
        CroatianBillQrCode expected = new CroatianBillQrCode(expectedDate, 5.0);
        assertEquals(actual, expected);
    }

    public void testCroatianOnlyDate() throws ParseException {
        String raw = "https://porezna.gov.hr/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1&datv=20230615_1045";
        CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
        LocalDateTime expectedDate = LocalDateTime.of(2023, 6, 15, 10, 45);
        CroatianBillQrCode expected = new CroatianBillQrCode(expectedDate, 0.0);
        assertEquals(actual, expected);
    }

    public void testCroatianOnlyAmount() throws ParseException {
        String raw = "https://porezna.gov.hr/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1&izn=5,00";
        CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
        CroatianBillQrCode expected = new CroatianBillQrCode(null, 5.0);
        assertEquals(actual, expected);
    }

    public void testCroatianBadUri() throws ParseException {
        String raw = "https://example.com";
        try {
            CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
            fail();
        } catch (ParseException ignored) {
            // should throw
        }
    }

    public void testCroatianMissingRelevantFields() throws ParseException {
        String raw = "https://porezna.gov.hr/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1";
        try {
            CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
            fail();
        } catch (ParseException ignored) {
            // should throw
        }
    }

    public void testCroatianBadHost() throws ParseException {
        String raw = "https://example.com/rn/?jir=7d6da168-bc2d-47c0-a238-1dbd9f5a74d1&datv=20230615_1045&izn=5,00";
        try {
            CroatianBillQrCode actual = BillParser.parseCroatianBillFromQrCode(raw);
            fail();
        } catch (ParseException ignored) {
            // should throw
        }
    }
}
