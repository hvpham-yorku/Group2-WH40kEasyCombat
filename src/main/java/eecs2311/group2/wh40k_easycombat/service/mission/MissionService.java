package eecs2311.group2.wh40k_easycombat.service.mission;

import eecs2311.group2.wh40k_easycombat.model.mission.MissionCard;
import eecs2311.group2.wh40k_easycombat.model.mission.MissionType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MissionService {
    private static final MissionService INSTANCE = new MissionService();

    private final List<MissionCard> primaryMissions;
    private final List<MissionCard> secondaryMissions;

    public static MissionService getInstance() {
        return INSTANCE;
    }

    private MissionService() {
        primaryMissions = load("/mission/PrimaryMission.txt", MissionType.PRIMARY);
        secondaryMissions = load("/mission/SecondaryMission.txt", MissionType.SECONDARY);
    }

    public List<MissionCard> getPrimaryMissions() {
        return List.copyOf(primaryMissions);
    }

    public List<MissionCard> getSecondaryMissions() {
        return List.copyOf(secondaryMissions);
    }

    public MissionCard findPrimaryByTitle(String title) {
        return findByTitle(primaryMissions, title);
    }

    public MissionCard findSecondaryByTitle(String title) {
        return findByTitle(secondaryMissions, title);
    }

    private List<MissionCard> load(String resourcePath, MissionType type) {
        List<String> lines = readLines(resourcePath);
        List<MissionCard> cards = new ArrayList<>();
        List<String> currentBlock = new ArrayList<>();

        for (String line : lines) {
            if (line.equalsIgnoreCase(type.label())) {
                if (!currentBlock.isEmpty()) {
                    MissionCard card = buildCard(type, currentBlock);
                    if (card != null) {
                        cards.add(card);
                    }
                    currentBlock.clear();
                }
                continue;
            }

            if (!line.isBlank()) {
                currentBlock.add(line);
            }
        }

        if (!currentBlock.isEmpty()) {
            MissionCard card = buildCard(type, currentBlock);
            if (card != null) {
                cards.add(card);
            }
        }

        return cards;
    }

    private MissionCard buildCard(MissionType type, List<String> block) {
        if (block == null || block.size() < 2) {
            return null;
        }

        String title = block.get(0);
        String intro = block.size() > 1 ? block.get(1) : "";
        List<String> bodyLines = block.size() > 2
                ? new ArrayList<>(block.subList(2, block.size()))
                : List.of();

        return new MissionCard(type, title, intro, bodyLines);
    }

    private List<String> readLines(String resourcePath) {
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("Mission resource not found: " + resourcePath);
        }

        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalize(line);
                if (!normalized.isBlank()) {
                    result.add(normalized);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read mission resource " + resourcePath, e);
        }
        return result;
    }

    private MissionCard findByTitle(List<MissionCard> source, String title) {
        String normalized = normalizeTitle(title);
        for (MissionCard card : source) {
            if (card != null && normalizeTitle(card.title()).equals(normalized)) {
                return card;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value
                .replace("\t", " ")
                .replace("鈥橲", "'s")
                .replace("鈥檚", "'s")
                .replace("鈥", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'")
                .replace("–", "-")
                .replace("—", "-")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.equals("|")) {
            return "";
        }

        return normalized;
    }

    private String normalizeTitle(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
