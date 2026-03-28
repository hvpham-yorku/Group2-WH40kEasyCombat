package eecs2311.group2.wh40k_easycombat.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WarhammerCommunityNewsService {

    public static final String NEWS_PAGE_URL =
            "https://www.warhammer-community.com/en-gb/setting/warhammer-40000/";

    private static final Pattern ARTICLE_LINK_PATTERN =
            Pattern.compile("<a[^>]+href=\"([^\"]*(?:/articles/|/en-gb/articles/)[^\"]*)\"[^>]*>(.*?)</a>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern DATE_PATTERN =
            Pattern.compile("\\b\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{2}\\b");

    private WarhammerCommunityNewsService() {
    }

    public static List<NewsArticle> fetchLatestNews(int limit) throws IOException {
        String html = downloadHtml(NEWS_PAGE_URL);
        return extractArticles(html, limit);
    }

    private static String downloadHtml(String urlText) throws IOException {
        @SuppressWarnings("deprecation")
		HttpURLConnection connection = (HttpURLConnection) new URL(urlText).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(4000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent", "WH40KEasyCombat/1.0");
        connection.setRequestProperty("Accept-Language", "en-GB,en;q=0.9");

        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<NewsArticle> extractArticles(String html, int limit) {
        if (html == null || html.isBlank() || limit <= 0) {
            return List.of();
        }

        Matcher matcher = ARTICLE_LINK_PATTERN.matcher(html);
        Map<String, NewsArticle> uniqueArticles = new LinkedHashMap<>();

        while (matcher.find() && uniqueArticles.size() < limit) {
            String href = normalizeUrl(matcher.group(1));
            String title = cleanTitle(matcher.group(2));

            if (href.isBlank() || title.isBlank() || title.length() < 8) {
                continue;
            }

            String normalizedTitle = title.toLowerCase(Locale.ROOT);
            if (uniqueArticles.containsKey(normalizedTitle)) {
                continue;
            }

            String trailingHtml = html.substring(matcher.end(), Math.min(html.length(), matcher.end() + 500));
            String publishedDate = findDate(trailingHtml);
            uniqueArticles.put(normalizedTitle, new NewsArticle(title, href, publishedDate));
        }

        return new ArrayList<>(uniqueArticles.values());
    }

    private static String findDate(String htmlSegment) {
        if (htmlSegment == null || htmlSegment.isBlank()) {
            return "";
        }

        String plainText = stripTags(htmlSegment);
        Matcher dateMatcher = DATE_PATTERN.matcher(plainText);
        return dateMatcher.find() ? dateMatcher.group() : "";
    }

    private static String normalizeUrl(String href) {
        String safeHref = href == null ? "" : href.trim();
        if (safeHref.isBlank()) {
            return "";
        }
        if (safeHref.startsWith("http://") || safeHref.startsWith("https://")) {
            return safeHref;
        }
        if (safeHref.startsWith("/")) {
            return "https://www.warhammer-community.com" + safeHref;
        }
        return "https://www.warhammer-community.com/" + safeHref;
    }

    private static String cleanTitle(String htmlTitle) {
        String cleaned = stripTags(htmlTitle)
                .replace("&amp;", "&")
                .replace("&#8217;", "'")
                .replace("&#8211;", "-")
                .replace("&nbsp;", " ")
                .replace("&#038;", "&")
                .replaceAll("\\s+", " ")
                .trim();

        return cleaned;
    }

    private static String stripTags(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ");
    }

    public record NewsArticle(String title, String url, String publishedDate) {
    }
}
