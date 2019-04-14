package ru.appliedtech.chess.elorating.changes.site;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.elorating.PlayerEloRating;
import ru.appliedtech.chess.elorating.PlayerEloRatingChange;
import ru.appliedtech.chess.elorating.PlayerKValueSet;
import ru.appliedtech.chess.elorating.EloRatingTable;
import ru.appliedtech.chess.elorating.EloratingChanges;
import ru.appliedtech.chess.elorating.changes.site.view.EloRatingTableView;
import ru.appliedtech.chess.elorating.changes.site.view.EloRatingTableViewHtmlRenderingEngine;
import ru.appliedtech.chess.elorating.io.PlayerEloRatingDeserializer;
import ru.appliedtech.chess.elorating.io.PlayerEloRatingSerializer;
import ru.appliedtech.chess.elorating.io.PlayerKValueSetDeserializer;
import ru.appliedtech.chess.elorating.io.PlayerKValueSetSerializer;
import ru.appliedtech.chess.storage.GameReadOnlyStorage;
import ru.appliedtech.chess.storage.GameStorage;
import ru.appliedtech.chess.storage.PlayerReadOnlyStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyMap;

public class EloRatingChangesSiteGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        new EloRatingChangesSiteGenerator().run(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7]);
    }

    private void run(String localeDef,
                     String playersFilePath,
                     String kValuesFilePath,
                     String baselineRatingsFilePath,
                     String gamesFilePath,
                     String newRatingsFilePath,
                     String ratingChangesFilePath,
                     String outputDir) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        new File(outputDir).mkdirs();
        Locale locale = resolveLocale(localeDef);

        PlayerReadOnlyStorage playerStorage = readPlayers(playersFilePath);
        GameStorage gameStorage = readGames(gamesFilePath);
        List<PlayerKValueSet> playerKValueSets = readKValues(kValuesFilePath);
        List<PlayerEloRating> baselineEloRatings = readEloRatings(baselineRatingsFilePath);

        EloratingChanges eloratingChanges = new EloratingChanges();
        List<PlayerEloRatingChange> playerEloRatingChanges = eloratingChanges.evaluateChanges(
                gameStorage.getGames(), playerKValueSets, baselineEloRatings);
        writeEloRatingChanges(ratingChangesFilePath, playerEloRatingChanges);
        List<PlayerEloRating> newRatings = eloratingChanges.evaluateNewRatings(
                baselineEloRatings, playerEloRatingChanges);
        writeEloRatings(newRatingsFilePath, newRatings);

        writeTable(playerStorage.getPlayers(), playerEloRatingChanges, newRatings, locale, outputDir);
    }

    private void writeTable(List<Player> players,
                            List<PlayerEloRatingChange> playerEloRatingChanges,
                            List<PlayerEloRating> newRatings,
                            Locale locale,
                            String outputDir) throws IOException {
        EloRatingTable eloRatingChangesTable = new EloRatingTable(
                players, playerEloRatingChanges, newRatings);
        EloRatingTableView view = new EloRatingTableView(locale, eloRatingChangesTable);
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(
                        new File(outputDir, "index.html")), StandardCharsets.UTF_8)) {
            new EloRatingTableViewHtmlRenderingEngine(createTemplatesConfiguration()).render(view, writer);
            writer.flush();
        }
    }

    private static void writeEloRatings(String ratingsFilePath, List<PlayerEloRating> ratings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ratings");
        module.addSerializer(new PlayerEloRatingSerializer());
        module.addDeserializer(PlayerEloRating.class, new PlayerEloRatingDeserializer());
        mapper.registerModule(module);

        try (FileOutputStream fos = new FileOutputStream(ratingsFilePath)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, ratings);
        }
    }

    private static void writeEloRatingChanges(String ratingChangesFilePath,
                                              List<PlayerEloRatingChange> playerEloRatingChanges) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("kvalues");
        module.addSerializer(new PlayerKValueSetSerializer());
        module.addDeserializer(PlayerKValueSet.class, new PlayerKValueSetDeserializer());
        mapper.registerModule(module);
        try (FileOutputStream fos = new FileOutputStream(ratingChangesFilePath)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, playerEloRatingChanges);
        }
    }

    private List<PlayerKValueSet> readKValues(String kValueStorageFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("kvalues");
        module.addSerializer(new PlayerKValueSetSerializer());
        module.addDeserializer(PlayerKValueSet.class, new PlayerKValueSetDeserializer());
        mapper.registerModule(module);
        List<PlayerKValueSet> records;
        try (FileInputStream fis = new FileInputStream(kValueStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<PlayerKValueSet>>() {});
        }
        return records;
    }

    private List<PlayerEloRating> readEloRatings(String eloRatingStorageFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ratings");
        module.addSerializer(new PlayerEloRatingSerializer());
        module.addDeserializer(PlayerEloRating.class, new PlayerEloRatingDeserializer());
        mapper.registerModule(module);

        List<PlayerEloRating> records;
        try (FileInputStream fis = new FileInputStream(eloRatingStorageFilePath)) {
            records = mapper.readValue(fis, new TypeReference<ArrayList<PlayerEloRating>>() {});
        }
        return records;
    }

    private PlayerReadOnlyStorage readPlayers(String playersFilePath) throws IOException {
        ObjectMapper baseMapper = new ChessBaseObjectMapper(emptyMap());
        List<Player> players;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            players = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        return new PlayerReadOnlyStorage(players);
    }

    private GameStorage readGames(String gamesFilePath) throws IOException {
        ObjectMapper gameObjectMapper = new GameObjectMapper(GameResultSystem.STANDARD);
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(gamesFilePath)) {
            games = gameObjectMapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }
        return new GameReadOnlyStorage(games);
    }

    private Configuration createTemplatesConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassForTemplateLoading(EloRatingChangesSiteGenerator.class, "/");
        return configuration;
    }

    private Locale resolveLocale(String localeDef) {
        String[] strings = localeDef.split("_");
        String language = strings.length > 0 ? strings[0] : "";
        String country = strings.length > 1 ? strings[1] : "";
        String variant = strings.length > 2 ? strings[2] : "";
        return language.isEmpty() ? Locale.US : new Locale(language, country, variant);
    }
}
