package ru.appliedtech.chess.elorating.changes.site.view;

import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.elorating.EloRating;
import ru.appliedtech.chess.elorating.EloRatingTable;
import ru.appliedtech.chess.elorating.changes.site.model.*;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Comparator.comparing;

public class EloRatingTableView {
    private static final EloRating NO_RATING = new EloRating(BigDecimal.ZERO);
    private final ResourceBundle resourceBundle;
    private final HeaderRowView headerRowView;
    private final List<PlayerRowView> playerRowViews;

    public EloRatingTableView(Locale locale,
                              EloRatingTable eloRatingTable) {
        this.resourceBundle = ResourceBundle.getBundle("resources", locale);
        this.headerRowView = createHeaderRowView();
        this.playerRowViews = createPlayerRowViews(eloRatingTable);
    }

    public String getTitle() {
        return resourceBundle.getString("elorating.table.view.title");
    }

    public HeaderRowView getHeaderRowView() {
        return headerRowView;
    }

    public List<PlayerRowView> getPlayerRowViews() {
        return playerRowViews;
    }

    private HeaderRowView createHeaderRowView() {
        List<HeaderCell> headerCells = new ArrayList<>();
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.player")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.rapid.games")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.rapid.change")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.rapid.rating")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.blitz.games")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.blitz.change")));
        headerCells.add(new HeaderCell(resourceBundle.getString("elorating.table.view.header.blitz.rating")));
        return new HeaderRowView(headerCells);
    }

    private List<PlayerRowView> createPlayerRowViews(EloRatingTable eloRatingTable) {
        List<PlayerRowView> rowViews = new ArrayList<>();
        List<Player> players = new ArrayList<>(eloRatingTable.getPlayers());
        Comparator<Player> rapidRatingComparator = comparing(p -> eloRatingTable.getRapidRating(p.getId()).orElse(NO_RATING));
        Comparator<Player> blitzRatingComparator = comparing(p -> eloRatingTable.getBlitzRating(p.getId()).orElse(NO_RATING));
        Comparator<Player> nameComparator = comparing(Player::getLastName).thenComparing(Player::getFirstName);
        players.sort(rapidRatingComparator
                .thenComparing(blitzRatingComparator)
                .thenComparing(nameComparator)
                .reversed());
        for (Player player : players) {
            List<CellView> cells = new ArrayList<>();
            cells.add(new CellView(player.getLastName() + " " + player.getFirstName()));
            {
                cells.add(eloRatingTable.getRapidGamesCount(player.getId())
                        .map(count -> (CellView) new IntCellView(count))
                        .orElse(new CellView("-")));
                cells.add(eloRatingTable.getRapidRatingChange(player.getId())
                        .map(value -> (CellView) new RatingCellView(value.getValue()))
                        .orElse(new CellView("-")));
                cells.add(eloRatingTable.getRapidRating(player.getId())
                        .map(value -> (CellView) new RatingCellView(value.getValue()))
                        .orElse(new CellView("1500?")));
            }
            {
                cells.add(eloRatingTable.getBlitzGamesCount(player.getId())
                        .map(count -> (CellView) new IntCellView(count))
                        .orElse(new CellView("-")));
                cells.add(eloRatingTable.getBlitzRatingChange(player.getId())
                        .map(value -> (CellView) new RatingCellView(value.getValue()))
                        .orElse(new CellView("-")));
                cells.add(eloRatingTable.getBlitzRating(player.getId())
                        .map(value -> (CellView) new RatingCellView(value.getValue()))
                        .orElse(new CellView("1500?")));
            }
            PlayerRowView playerRowView = new PlayerRowView(cells);
            rowViews.add(playerRowView);
        }

        return rowViews;
    }
}
