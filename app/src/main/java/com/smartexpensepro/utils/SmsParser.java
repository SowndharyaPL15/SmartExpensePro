package com.smartexpensepro.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    // Keywords that clearly indicate money going OUT of account
    private static final String[] DEBIT_KEYWORDS = {
            "debited", "spent", "withdrawn", "deducted",
            "purchase", "payment of", "paid to", "sent to",
            "transferred to", "charged"
    };

    // Keywords that indicate money coming IN — must be rejected immediately
    private static final String[] CREDIT_KEYWORDS = {
            "credited", "received", "deposited", "refund",
            "cashback", "credit of", "added to", "reversed"
    };

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?:Rs\\.?|INR)\\s?([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE);

    public static boolean isDebitSms(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();

        // Step 1 — Immediately reject if it is a credit/incoming SMS
        for (String creditWord : CREDIT_KEYWORDS) {
            if (lower.contains(creditWord)) return false;
        }

        // Step 2 — Accept only if it contains a debit keyword
        for (String debitWord : DEBIT_KEYWORDS) {
            if (lower.contains(debitWord)) return true;
        }

        return false;
    }

    public static double extractAmount(String message) {
        if (message == null) return 0;
        Matcher matcher = AMOUNT_PATTERN.matcher(message);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            try {
                return Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static String detectCategory(String message) {
        if (message == null) return "Others";
        String lower = message.toLowerCase();

        // ─────────────────────────────────────────
        // FOOD
        // ─────────────────────────────────────────
        if (lower.contains("zomato")
                || lower.contains("swiggy")
                || lower.contains("dominos")
                || lower.contains("domino")
                || lower.contains("pizzahut")
                || lower.contains("pizza hut")
                || lower.contains("kfc")
                || lower.contains("mcdonalds")
                || lower.contains("mcdonald")
                || lower.contains("burger king")
                || lower.contains("burgerking")
                || lower.contains("subway")
                || lower.contains("restaurant")
                || lower.contains("cafe")
                || lower.contains("coffee")
                || lower.contains("starbucks")
                || lower.contains("chaayos")
                || lower.contains("food")
                || lower.contains("biryani")
                || lower.contains("hotel")
                || lower.contains("diner")
                || lower.contains("bakery")
                || lower.contains("canteen")
                || lower.contains("dunkin")
                || lower.contains("baskinrobbins")
                || lower.contains("haldiram")
                || lower.contains("barbeque")
                || lower.contains("bbq")) {
            return "Food";
        }

        // ─────────────────────────────────────────
        // TRAVEL
        // ─────────────────────────────────────────
        if (lower.contains("uber")
                || lower.contains("ola")
                || lower.contains("rapido")
                || lower.contains("meru")
                || lower.contains("petrol")
                || lower.contains("fuel")
                || lower.contains("diesel")
                || lower.contains("hp petrol")
                || lower.contains("indian oil")
                || lower.contains("iocl")
                || lower.contains("hpcl")
                || lower.contains("bpcl")
                || lower.contains("metro")
                || lower.contains("railway")
                || lower.contains("irctc")
                || lower.contains("train")
                || lower.contains("flight")
                || lower.contains("airline")
                || lower.contains("indigo")
                || lower.contains("spicejet")
                || lower.contains("airindia")
                || lower.contains("air india")
                || lower.contains("goair")
                || lower.contains("vistara")
                || lower.contains("makemytrip")
                || lower.contains("goibibo")
                || lower.contains("yatra")
                || lower.contains("redbus")
                || lower.contains("bus")
                || lower.contains("cab")
                || lower.contains("taxi")
                || lower.contains("auto fare")
                || lower.contains("toll")
                || lower.contains("parking")
                || lower.contains("fastag")) {
            return "Travel";
        }

        // ─────────────────────────────────────────
        // BILLS & UTILITIES
        // ─────────────────────────────────────────
        if (lower.contains("electricity")
                || lower.contains("bescom")
                || lower.contains("tneb")
                || lower.contains("msedcl")
                || lower.contains("bses")
                || lower.contains("tata power")
                || lower.contains("adani electricity")
                || lower.contains("water bill")
                || lower.contains("gas bill")
                || lower.contains("indane")
                || lower.contains("hp gas")
                || lower.contains("bharat gas")
                || lower.contains("lpg")
                || lower.contains("recharge")
                || lower.contains("mobile recharge")
                || lower.contains("dth")
                || lower.contains("tata sky")
                || lower.contains("dish tv")
                || lower.contains("airtel")
                || lower.contains("jio")
                || lower.contains("vodafone")
                || lower.contains("vi ")
                || lower.contains("bsnl")
                || lower.contains("broadband")
                || lower.contains("internet")
                || lower.contains("wifi")
                || lower.contains("postpaid")
                || lower.contains("prepaid")
                || lower.contains("bill payment")
                || lower.contains("utility")) {
            return "Bills";
        }

        // ─────────────────────────────────────────
        // SHOPPING
        // ─────────────────────────────────────────
        if (lower.contains("amazon")
                || lower.contains("flipkart")
                || lower.contains("myntra")
                || lower.contains("meesho")
                || lower.contains("ajio")
                || lower.contains("nykaa")
                || lower.contains("snapdeal")
                || lower.contains("shopclues")
                || lower.contains("tatacliq")
                || lower.contains("reliance")
                || lower.contains("dmart")
                || lower.contains("bigbasket")
                || lower.contains("grofers")
                || lower.contains("blinkit")
                || lower.contains("zepto")
                || lower.contains("instamart")
                || lower.contains("swiggy instamart")
                || lower.contains("jiomart")
                || lower.contains("shopping")
                || lower.contains("supermarket")
                || lower.contains("hypermarket")
                || lower.contains("mall")
                || lower.contains("retail")
                || lower.contains("store")
                || lower.contains("bazaar")
                || lower.contains("market")
                || lower.contains("croma")
                || lower.contains("vijay sales")
                || lower.contains("poorvika")
                || lower.contains("decathlon")
                || lower.contains("lifestyle")
                || lower.contains("westside")
                || lower.contains("pantaloons")
                || lower.contains("max fashion")
                || lower.contains("zara")
                || lower.contains("h&m")) {
            return "Shopping";
        }

        // ─────────────────────────────────────────
        // HEALTH & MEDICAL
        // ─────────────────────────────────────────
        if (lower.contains("pharmacy")
                || lower.contains("medical")
                || lower.contains("hospital")
                || lower.contains("clinic")
                || lower.contains("doctor")
                || lower.contains("apollo")
                || lower.contains("fortis")
                || lower.contains("manipal")
                || lower.contains("mediplus")
                || lower.contains("netmeds")
                || lower.contains("1mg")
                || lower.contains("pharmeasy")
                || lower.contains("healthkart")
                || lower.contains("diagnostic")
                || lower.contains("lab test")
                || lower.contains("medicine")) {
            return "Health";
        }

        // ─────────────────────────────────────────
        // ENTERTAINMENT
        // ─────────────────────────────────────────
        if (lower.contains("netflix")
                || lower.contains("amazon prime")
                || lower.contains("hotstar")
                || lower.contains("disney")
                || lower.contains("zee5")
                || lower.contains("sonyliv")
                || lower.contains("jiocinema")
                || lower.contains("spotify")
                || lower.contains("gaana")
                || lower.contains("wynk")
                || lower.contains("youtube premium")
                || lower.contains("bookmyshow")
                || lower.contains("movie")
                || lower.contains("cinema")
                || lower.contains("pvr")
                || lower.contains("inox")
                || lower.contains("multiplex")
                || lower.contains("gaming")
                || lower.contains("playstation")
                || lower.contains("xbox")) {
            return "Entertainment";
        }

        // ─────────────────────────────────────────
        // EDUCATION
        // ─────────────────────────────────────────
        if (lower.contains("school fee")
                || lower.contains("college fee")
                || lower.contains("tuition")
                || lower.contains("byju")
                || lower.contains("unacademy")
                || lower.contains("coursera")
                || lower.contains("udemy")
                || lower.contains("whitehat")
                || lower.contains("vedantu")
                || lower.contains("education")
                || lower.contains("exam fee")
                || lower.contains("books")
                || lower.contains("stationery")) {
            return "Education";
        }

        return "Others";
    }
}
