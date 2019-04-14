package ru.appliedtech.chess.elorating.changes.site.view;

import java.io.IOException;
import java.io.Writer;

public interface EloRatingTableViewRenderingEngine {
    void render(EloRatingTableView playoffLevelTableView, Writer writer) throws IOException;
}
