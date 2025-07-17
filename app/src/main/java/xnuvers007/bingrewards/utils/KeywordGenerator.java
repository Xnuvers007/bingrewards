package xnuvers007.bingrewards.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KeywordGenerator {

    private Random random = new Random();

    // Natural keyword categories
    private final List<String> categories = Arrays.asList(
            "technology", "science", "health", "food", "travel", "sports",
            "movies", "books", "music", "art", "history", "nature"
    );

    private final List<String> techKeywords = Arrays.asList(
            "artificial intelligence", "machine learning", "blockchain", "cybersecurity",
            "cloud computing", "mobile apps", "web development", "data science",
            "quantum computing", "robotics", "internet of things", "virtual reality"
    );

    private final List<String> scienceKeywords = Arrays.asList(
            "climate change", "space exploration", "genetics", "medicine",
            "physics discoveries", "chemistry research", "biology studies", "astronomy",
            "environmental science", "renewable energy", "scientific method", "research"
    );

    private final List<String> healthKeywords = Arrays.asList(
            "healthy eating", "exercise routines", "mental health", "nutrition",
            "fitness tips", "wellness", "medical research", "healthcare",
            "disease prevention", "lifestyle changes", "vitamins", "meditation"
    );

    private final List<String> generalKeywords = Arrays.asList(
            "how to", "what is", "best practices", "tips and tricks", "guide",
            "tutorial", "review", "comparison", "latest news", "trends",
            "benefits of", "advantages", "disadvantages", "solutions"
    );

    public String generateRandomKeyword() {
        int categoryChoice = random.nextInt(5);

        switch (categoryChoice) {
            case 0:
                return generateTechKeyword();
            case 1:
                return generateScienceKeyword();
            case 2:
                return generateHealthKeyword();
            case 3:
                return generateCombinedKeyword();
            default:
                return generateGeneralKeyword();
        }
    }

    private String generateTechKeyword() {
        List<String> prefixes = Arrays.asList("", "latest ", "best ", "new ", "advanced ");
        List<String> suffixes = Arrays.asList("", " 2024", " trends", " guide", " tips");

        String prefix = prefixes.get(random.nextInt(prefixes.size()));
        String keyword = techKeywords.get(random.nextInt(techKeywords.size()));
        String suffix = suffixes.get(random.nextInt(suffixes.size()));

        return (prefix + keyword + suffix).trim();
    }

    private String generateScienceKeyword() {
        List<String> prefixes = Arrays.asList("", "recent ", "latest ", "new ", "breakthrough ");
        String prefix = prefixes.get(random.nextInt(prefixes.size()));
        String keyword = scienceKeywords.get(random.nextInt(scienceKeywords.size()));

        return (prefix + keyword).trim();
    }

    private String generateHealthKeyword() {
        List<String> prefixes = Arrays.asList("", "effective ", "best ", "healthy ", "natural ");
        String prefix = prefixes.get(random.nextInt(prefixes.size()));
        String keyword = healthKeywords.get(random.nextInt(healthKeywords.size()));

        return (prefix + keyword).trim();
    }

    private String generateCombinedKeyword() {
        String general = generalKeywords.get(random.nextInt(generalKeywords.size()));
        String category = categories.get(random.nextInt(categories.size()));

        return general + " " + category;
    }

    private String generateGeneralKeyword() {
        return generalKeywords.get(random.nextInt(generalKeywords.size())) + " " +
                categories.get(random.nextInt(categories.size()));
    }
}