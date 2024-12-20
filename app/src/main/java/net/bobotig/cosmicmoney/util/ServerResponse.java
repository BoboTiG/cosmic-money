package net.bobotig.cosmicmoney.util;

import android.util.Log;

import net.bobotig.cosmicmoney.model.DBAccountProject;
import net.bobotig.cosmicmoney.model.DBBill;
import net.bobotig.cosmicmoney.model.DBBillOwer;
import net.bobotig.cosmicmoney.model.DBCategory;
import net.bobotig.cosmicmoney.model.DBCurrency;
import net.bobotig.cosmicmoney.model.DBMember;
import net.bobotig.cosmicmoney.model.DBPaymentMode;
import net.bobotig.cosmicmoney.model.DBProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Provides entity classes for handling server responses
 */
public class ServerResponse {
    protected final boolean isOcsResponse;
    private static final String TAG = ServerResponse.class.getSimpleName();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

    public static class NotModifiedException extends IOException {
    }

    private final VersatileProjectSyncClient.ResponseData response;

    public ServerResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
        this.response = response;
        this.isOcsResponse = isOcsResponse;
    }

    protected String getContent() {
        return response.getContent();
    }

    public String getETag() {
        return response.getETag();
    }

    public long getLastModified() {
        return response.getLastModified();
    }

    public JSONObject getResponseObjectData() throws JSONException {
        JSONObject rawData = new JSONObject(getContent());
        if (!isOcsResponse) {
            return rawData;
        }
        JSONObject data = rawData.getJSONObject("ocs");
        return data.getJSONObject("data");
    }

    public JSONArray getResponseArrayData() throws JSONException {
        if (!isOcsResponse) {
            return new JSONArray(getContent());
        }
        JSONObject rawData = new JSONObject(getContent());
        JSONObject data = rawData.getJSONObject("ocs");
        return data.getJSONArray("data");
    }

    public String getResponseStringData() throws JSONException {
        if (!isOcsResponse) {
            return getContent();
        }
        JSONObject rawData = new JSONObject(getContent());
        JSONObject data = rawData.getJSONObject("ocs");
        return data.getString("data");
    }

    public static class ProjectResponse extends ServerResponse {
        public ProjectResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getEmail() throws JSONException {
            return getEmailFromJSON(getResponseObjectData());
        }

        public String getName() throws JSONException {
            return getNameFromJSON(getResponseObjectData());
        }

        public boolean getDeletionDisabled() throws JSONException {
            return getDeletionDisabledFromJSON(getResponseObjectData());
        }

        public int getMyAccessLevel() throws JSONException {
            return getMyAccessLevelFromJSON(getResponseObjectData());
        }

        public String getCurrencyName() throws JSONException {
            return getCurrencyNameFromJSON(getResponseObjectData());
        }

        public List<DBMember> getMembers(long projId) throws JSONException {
            return getMembersFromJSON(getResponseObjectData(), projId);
        }

        public List<DBCategory> getCategories(long projId) throws JSONException {
            return getCategoriesFromJSON(getResponseObjectData(), projId);
        }

        public List<DBPaymentMode> getPaymentModes(long projId) throws JSONException {
            return getPaymentModesFromJSON(getResponseObjectData(), projId);
        }

        public List<DBCurrency> getCurrencies(long projId) throws JSONException {
            return getCurrenciesFromJSON(getResponseObjectData(), projId);
        }
    }

    public static class CreateRemoteMemberResponse extends ServerResponse {
        private boolean isJsonMember;
        public CreateRemoteMemberResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse, boolean isJsonMember) {
            super(response, isOcsResponse);
            this.isJsonMember = isJsonMember;
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }

        public Long getRemoteMemberId() throws JSONException {
            return isJsonMember
                    ? getRemoteMemberIdFromJSON(getResponseObjectData())
                    : Long.parseLong(getResponseStringData());
        }
    }

    public static class CreateRemoteCurrencyResponse extends ServerResponse {
        public CreateRemoteCurrencyResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse){
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class EditRemoteCurrencyResponse extends ServerResponse {
        public EditRemoteCurrencyResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse){
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class DeleteRemoteCurrencyResponse extends ServerResponse {
        public DeleteRemoteCurrencyResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class EditRemoteProjectResponse extends ServerResponse {

        public EditRemoteProjectResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class EditRemoteMemberResponse extends ServerResponse {
        public EditRemoteMemberResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public long getRemoteId(long projectId) throws JSONException {
            return getMemberFromJSON(getResponseObjectData(), projectId)
                    .getRemoteId();
        }
    }

    public static class EditRemoteBillResponse extends ServerResponse {
        public EditRemoteBillResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class CreateRemoteBillResponse extends ServerResponse {
        public CreateRemoteBillResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class DeleteRemoteBillResponse extends ServerResponse {
        public DeleteRemoteBillResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class DeleteRemoteProjectResponse extends ServerResponse {
        public DeleteRemoteProjectResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class CreateRemoteProjectResponse extends ServerResponse {
        public CreateRemoteProjectResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public String getStringContent() throws JSONException {
            return getResponseStringData();
        }
    }

    public static class BillsResponse extends ServerResponse {
        public BillsResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public List<DBBill> getBillsIHM(long projId, Map<Long, Long> memberRemoteIdToId) throws JSONException {
            return getBillsFromJSONArray(new JSONArray(getContent()), projId, memberRemoteIdToId);
        }

        public List<DBBill> getBillsCospend(long projId, Map<Long, Long> memberRemoteIdToId) throws JSONException {
            return getBillsFromJSONObject(getResponseObjectData(), projId, memberRemoteIdToId);
        }

        public List<Long> getAllBillIds() throws JSONException {
            return getAllBillIdsFromJSON(getResponseObjectData());
        }

        public Long getSyncTimestamp() throws JSONException {
            return getSyncTimestampFromJSON(getResponseObjectData());
        }
    }

    public static class MembersResponse extends ServerResponse {
        public MembersResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public List<DBMember> getMembers(long projId) throws JSONException {
            return getMembersFromJSONArray(getResponseArrayData(), projId);
        }
    }

    public static class AccountProjectsResponse extends ServerResponse {
        public AccountProjectsResponse(VersatileProjectSyncClient.ResponseData response, boolean isOcsResponse) {
            super(response, isOcsResponse);
        }

        public List<DBAccountProject> getAccountProjects(String ncUrl) throws JSONException {
            return getAccountProjectsFromJSONArray(getResponseArrayData(), ncUrl);
        }
    }

    public static class CapabilitiesResponse extends ServerResponse {
        public CapabilitiesResponse(VersatileProjectSyncClient.ResponseData response) {
            super(response, true);
        }

        public String getColor() throws IOException, JSONException {
            return getColorFromJsonContent(new JSONObject(getContent()));
        }

        public String getCospendVersion() throws JSONException {
            return getCospendVersionFromCapabilitiesContent(new JSONObject(getContent()));
        }
    }

    public static class AvatarResponse extends ServerResponse {
        public AvatarResponse(VersatileProjectSyncClient.ResponseData response) {
            super(response, false);
        }

        public String getAvatarString() throws IOException {
            return getContent();
        }
    }

    protected String getPublicTokenFromJSON(JSONObject json) throws JSONException {
        int done = 0;
        String publictoken;
        if (json.has("code") && json.has("sharetoken")) {
            done = json.getInt("code");
            publictoken = json.getString("sharetoken");
            if (done == 1) {
                return publictoken;
            }
        }
        return null;
    }

    protected String getNameFromJSON(JSONObject json) throws JSONException {
        String name = "";
        if (json.has("name")) {
            name = json.getString("name");
        }
        return name;
    }

    protected boolean getDeletionDisabledFromJSON(JSONObject json) throws JSONException {
        boolean deletionDisabled = false;
        if (json.has("deletiondisabled")) {
            deletionDisabled = json.getBoolean("deletiondisabled");
        }
        return deletionDisabled;
    }

    protected int getMyAccessLevelFromJSON(JSONObject json) throws JSONException {
        int myAccessLevel = DBProject.ACCESS_LEVEL_UNKNOWN;
        if (json.has("myaccesslevel")) {
            myAccessLevel = json.getInt("myaccesslevel");
        }
        return myAccessLevel;
    }

    protected String getCurrencyNameFromJSON(JSONObject json) throws JSONException {
        String currencyName = "";
        if (json.has("currencyname")) {
            currencyName = json.getString("currencyname");
        }
        return currencyName;
    }

    protected String getEmailFromJSON(JSONObject json) throws JSONException {
        String email = "";
        if (json.has("contact_email")) {
            email = json.getString("contact_email");
        }
        return email;
    }

    protected List<DBMember> getMembersFromJSONArray(JSONArray jsonMs, long projId) throws JSONException {
        List<DBMember> members = new ArrayList<>();
        for (int i = 0; i < jsonMs.length(); i++) {
            JSONObject jsonM = jsonMs.getJSONObject(i);
            members.add(getMemberFromJSON(jsonM, projId));
        }

        return members;
    }

    protected List<DBCategory> getCategoriesFromJSON(JSONObject json, long projId) throws JSONException {
        List<DBCategory> categories = new ArrayList<>();

        if (json.has("categories") && json.get("categories") instanceof JSONObject) {
            JSONObject jsonCats = json.getJSONObject("categories");
            Iterator<String> keys = jsonCats.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonCats.get(key) instanceof JSONObject) {
                    categories.add(getCategoryFromJSON(jsonCats.getJSONObject(key), key, projId));
                }
            }
        }
        return categories;
    }

    protected DBCategory getCategoryFromJSON(JSONObject json, String remoteIdStr, long projId) throws JSONException {
        long remoteId = Long.valueOf(remoteIdStr);
        String name = "";
        String color = "";
        String icon = "";
        if (json.has("color") && !json.isNull("color")) {
            color = json.getString("color");
        }
        if (json.has("icon") && !json.isNull("icon")) {
            icon = json.getString("icon");
        }
        if (json.has("name") && !json.isNull("name")) {
            name = json.getString("name");
        }
        return new DBCategory(0, remoteId, projId, name, icon, color);
    }

    protected List<DBPaymentMode> getPaymentModesFromJSON(JSONObject json, long projId) throws JSONException {
        List<DBPaymentMode> paymentModes = new ArrayList<>();

        if (json.has("paymentmodes") && json.get("paymentmodes") instanceof JSONObject) {
            JSONObject jsonPms = json.getJSONObject("paymentmodes");
            Iterator<String> keys = jsonPms.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonPms.get(key) instanceof JSONObject) {
                    paymentModes.add(getPaymentModeFromJSON(jsonPms.getJSONObject(key), key, projId));
                }
            }
        }
        return paymentModes;
    }

    protected DBPaymentMode getPaymentModeFromJSON(JSONObject json, String remoteIdStr, long projId) throws JSONException {
        long remoteId = Long.valueOf(remoteIdStr);
        String name = "";
        String color = "";
        String icon = "";
        if (json.has("color") && !json.isNull("color")) {
            color = json.getString("color");
        }
        if (json.has("icon") && !json.isNull("icon")) {
            icon = json.getString("icon");
        }
        if (json.has("name") && !json.isNull("name")) {
            name = json.getString("name");
        }
        return new DBPaymentMode(0, remoteId, projId, name, icon, color);
    }

    protected List<DBCurrency> getCurrenciesFromJSON(JSONObject json, long projId) throws JSONException {
        List<DBCurrency> currencies = new ArrayList<>();

        if (json.has("currencies") && json.get("currencies") instanceof JSONArray) {
            JSONArray jsonCurs = json.getJSONArray("currencies");
            for (int i = 0; i < jsonCurs.length(); i++) {
                if (jsonCurs.get(i) instanceof JSONObject) {
                    currencies.add(getCurrencyFromJSON(jsonCurs.getJSONObject(i), projId));
                }
            }
        }
        return currencies;
    }

    protected DBCurrency getCurrencyFromJSON(JSONObject json, long projId) throws JSONException {
        long remoteId = 0;
        String name = "";
        Double exchangeRate = 1.0;
        if (json.has("exchange_rate") && !json.isNull("exchange_rate")) {
            exchangeRate = json.getDouble("exchange_rate");
        }
        if (json.has("id") && !json.isNull("id")) {
            remoteId = json.getLong("id");
        }
        if (json.has("name") && !json.isNull("name")) {
            name = json.getString("name");
        }
        return new DBCurrency(0, remoteId, projId, name, exchangeRate, DBBill.STATE_OK);
    }

    protected List<DBMember> getMembersFromJSON(JSONObject json, long projId) throws JSONException {
        List<DBMember> members = new ArrayList<>();

        if (json.has("members")) {
            JSONArray jsonMs = json.getJSONArray("members");
            for (int i = 0; i < jsonMs.length(); i++) {
                JSONObject jsonM = jsonMs.getJSONObject(i);
                members.add(getMemberFromJSON(jsonM, projId));
            }
        }
        return members;
    }

    protected DBMember getMemberFromJSON(JSONObject json, long projId) throws JSONException {
        boolean activated = true;
        double weight = 1;
        long remoteId = 0;
        String name = "";
        Integer r = null;
        Integer g = null;
        Integer b = null;
        String ncUserId = null;
        if (!json.isNull("id")) {
            remoteId = json.getLong("id");
        }
        if (!json.isNull("weight")) {
            weight = json.getDouble("weight");
        }
        if (!json.isNull("activated")) {
            activated = json.getBoolean("activated");
        }
        if (!json.isNull("name")) {
            name = json.getString("name");
        }
        if (json.has("color") && !json.isNull("color")) {
            Object obj = json.get("color");
            if (obj instanceof String) {
                String color = json.getString("color").replace("#", "");
                if (color.length() == 6) {
                    r = Integer.parseInt(color.substring(0,2), 16);
                    g = Integer.parseInt(color.substring(2,4), 16);
                    b = Integer.parseInt(color.substring(4,6), 16);
                }
            } else if (obj instanceof JSONObject) {
                JSONObject color = json.getJSONObject("color");
                if (color.has("r") && !color.isNull("r")) {
                    r = color.getInt("r");
                }
                if (color.has("g") && !color.isNull("g")) {
                    g = color.getInt("g");
                }
                if (color.has("b") && !color.isNull("b")) {
                    b = color.getInt("b");
                }
            }
        }
        if (json.has("userid") && !json.isNull("userid")) {
            ncUserId = json.getString("userid");
        }
        return new DBMember(
            0, remoteId, projId, name, activated, weight, DBBill.STATE_OK,
            r, g, b, ncUserId, null
        );
    }

    protected List<Long> getAllBillIdsFromJSON(JSONObject json) throws JSONException {
        List<Long> billIds = new ArrayList<>();
        if (json.has("allBillIds") && !json.isNull("allBillIds")) {
            JSONArray jsonBillIds = json.getJSONArray("allBillIds");
            for (int i = 0; i < jsonBillIds.length(); i++) {
                billIds.add(jsonBillIds.getLong(i));
                //Log.e("billid", "BILLID " + jsonBillIds.getLong(i));
            }
        }
        return billIds;
    }

    protected Long getSyncTimestampFromJSON(JSONObject json) throws JSONException {
        Long ts = Long.valueOf(0);
        if (json.has("timestamp") && !json.isNull("timestamp")) {
            ts = json.getLong("timestamp");
        }
        return ts;
    }

    protected List<DBBill> getBillsFromJSONArray(JSONArray json, long projId, Map<Long, Long> memberRemoteIdToId) throws JSONException {
        List<DBBill> bills = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonBill = json.getJSONObject(i);
            bills.add(getBillFromJSON(jsonBill, projId, memberRemoteIdToId));
        }
        return bills;
    }

    protected List<DBBill> getBillsFromJSONObject(JSONObject json, long projId, Map<Long, Long> memberRemoteIdToId) throws JSONException {
        List<DBBill> bills;
        if (json.has("bills") && !json.isNull("bills")) {
            JSONArray jsonBills = json.getJSONArray("bills");
            bills = getBillsFromJSONArray(jsonBills, projId, memberRemoteIdToId);
        } else {
            bills = new ArrayList<>();
        }
        return bills;
    }

    protected DBBill getBillFromJSON(JSONObject json, long projId, Map<Long, Long> memberRemoteIdToId) throws JSONException {
        long remoteId = 0;
        long payerRemoteId = 0;
        long payerId = 0;
        double amount = 0;
        String dateStr = "";
        Date date;
        long timestamp = 0;
        String what = "";
        String comment = "";
        String repeat = DBBill.NON_REPEATED;
        String paymentMode = DBBill.PAYMODE_NONE;
        int paymentModeRemoteId = DBBill.PAYMODE_ID_NONE;
        int categoryId = DBBill.CATEGORY_NONE;
        if (!json.isNull("id")) {
            remoteId = json.getLong("id");
        }
        if (!json.isNull("payer_id")) {
            payerRemoteId = json.getLong("payer_id");
            payerId = memberRemoteIdToId.get(payerRemoteId);
            //Log.w(ServerResponse.class.getSimpleName(), "WTF : "+payerId);
        }
        if (!json.isNull("amount")) {
            amount = json.getDouble("amount");
        }
        // get timestamp in priority
        if (!json.isNull("timestamp")) {
            timestamp = json.getLong("timestamp");
        } else if (!json.isNull("date")) {
            dateStr = json.getString("date");
            try {
                date = sdf.parse(dateStr);
                timestamp = date.getTime() / 1000;
            } catch (Exception e) {
                timestamp = 0;
            }
        }
        if (!json.isNull("what")) {
            what = json.getString("what");
        }
        if (!json.isNull("comment")) {
            comment = json.getString("comment");
        }
        if (json.has("repeat") && !json.isNull("repeat")) {
            repeat = json.getString("repeat");
        }
        if (json.has("paymentmode") && !json.isNull("paymentmode")) {
            paymentMode = json.getString("paymentmode");
        }
        if (json.has("categoryid") && !json.isNull("categoryid")) {
            categoryId = json.getInt("categoryid");
            Log.d("PLOP", "LOADED CATTTTTTTTTTTT " + categoryId);
        }
        if (json.has("paymentmodeid") && !json.isNull("paymentmodeid")) {
            paymentModeRemoteId = json.getInt("paymentmodeid");
        }
        // old MB, new Cospend is ok as Cospend provides the old pm ID
        // new MB, old Cospend => set payment mode ID from old one
        if (!DBBill.PAYMODE_NONE.equals(paymentMode)
                && !"".equals(paymentMode)
                && paymentModeRemoteId == DBBill.PAYMODE_ID_NONE) {
            Log.d("PaymentMode", "old: " + paymentMode + " and new: " + paymentModeRemoteId);
            paymentModeRemoteId = DBBill.oldPmIdToNew.get(paymentMode);
        }
        DBBill bill = new DBBill(
            0, remoteId, projId, payerId, amount, timestamp, what,
            DBBill.STATE_OK, repeat, paymentMode, categoryId, comment, paymentModeRemoteId
        );
        bill.setBillOwers(getBillOwersFromJson(json, memberRemoteIdToId));
        return bill;
    }

    protected List<DBBillOwer> getBillOwersFromJson(JSONObject json, Map<Long, Long> memberRemoteIdToId) throws JSONException {
        List<DBBillOwer> billOwers = new ArrayList<>();

        if (json.has("owers")) {
            JSONArray jsonOs = json.getJSONArray("owers");
            for (int i = 0; i < jsonOs.length(); i++) {
                JSONObject jsonO = jsonOs.getJSONObject(i);
                long memberRemoteId = jsonO.getLong("id");
                long memberLocalId = memberRemoteIdToId.get(memberRemoteId);
                billOwers.add(new DBBillOwer(0,0, memberLocalId));
            }
        }
        return billOwers;
    }

    protected List<DBAccountProject> getAccountProjectsFromJSONArray(JSONArray jsonAPs, String ncUrl) throws JSONException {
        List<DBAccountProject> accountProjects = new ArrayList<>();
        for (int i = 0; i < jsonAPs.length(); i++) {
            JSONObject jsonAP = jsonAPs.getJSONObject(i);
            accountProjects.add(getAccountProjectFromJSON(jsonAP, ncUrl));
        }

        return accountProjects;
    }

    protected DBAccountProject getAccountProjectFromJSON(JSONObject json, String accountNcUrl) throws JSONException {
        String remoteId = "";
        String name = "";
        String ncUrl = "";

        if (!json.isNull("name")) {
            name = json.getString("name");
        }
        if (!json.isNull("id")) {
            remoteId = json.getString("id");
        }
        if (!json.isNull("ncurl")) {
            ncUrl = json.getString("ncUrl");
        }
        if (ncUrl.isEmpty()) {
            ncUrl = accountNcUrl;
        }
        return new DBAccountProject(0, remoteId, null, name, ncUrl);
    }

    protected String getColorFromContent(String content) throws IOException {
        //Log.i(TAG, "CONCON" + content);
        String result = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream stream = new ByteArrayInputStream(content.getBytes());
            Document doc = db.parse(stream);
            doc.getDocumentElement().normalize();
            // Locate the Tag Name
            NodeList nodelist = doc.getElementsByTagName("color");
            if (nodelist.getLength() > 0) {
                result = nodelist.item(0).getTextContent();
                Log.i(TAG,"I GOT THE COLOR from server: "+result);
            }
        }
        catch (ParserConfigurationException e) {
        }
        catch (SAXException e) {
        }
        return result;
    }

    protected String getColorFromJsonContent(JSONObject json) {
        try {
            JSONObject ocs = json.getJSONObject("ocs");
            JSONObject data = ocs.getJSONObject("data");
            JSONObject capabilities = data.getJSONObject("capabilities");
            JSONObject theming = capabilities.getJSONObject("theming");
            String color = theming.getString("color");
            Log.i(TAG,"I GOT THE COLOR from server's JSON response: "+color);
            return color;
        } catch (JSONException e) {
            Log.e(TAG,"Failed to get the color from OCS capabilities response " + e);
        }
        return null;
    }

    protected String getCospendVersionFromCapabilitiesContent(JSONObject json) {
        try {
            JSONObject ocs = json.getJSONObject("ocs");
            JSONObject data = ocs.getJSONObject("data");
            JSONObject capabilities = data.getJSONObject("capabilities");
            JSONObject cospend = capabilities.getJSONObject("cospend");
            String version = cospend.getString("version");
            Log.i(TAG,"I GOT THE Cospend version: "+version);
            return version;
        } catch (JSONException e) {
            Log.i(TAG,"Failed to get the Cospend version"+e);
        }
        return null;
    }

    protected Long getRemoteMemberIdFromJSON(JSONObject json) throws JSONException {
        return json.getLong("id");
    }
}
