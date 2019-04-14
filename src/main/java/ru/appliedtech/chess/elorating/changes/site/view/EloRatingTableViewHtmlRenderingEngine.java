package ru.appliedtech.chess.elorating.changes.site.view;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class EloRatingTableViewHtmlRenderingEngine implements EloRatingTableViewRenderingEngine {
    private final Configuration templatesConfiguration;

    public EloRatingTableViewHtmlRenderingEngine(Configuration templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    @Override
    public void render(EloRatingTableView playoffLevelTableView, Writer writer) throws IOException {
        Template template = templatesConfiguration.getTemplate("index.ftl");
        try {
            template.process(playoffLevelTableView, writer);
            writer.flush();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }
}
