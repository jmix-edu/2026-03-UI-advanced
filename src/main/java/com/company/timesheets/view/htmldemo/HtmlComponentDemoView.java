package com.company.timesheets.view.htmldemo;

import com.company.timesheets.view.main.MainView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "html-component-demo", layout = MainView.class)
@ViewController("ts_HtmlComponentDemoView")
@ViewDescriptor("html-component-demo-view.xml")
public class HtmlComponentDemoView extends StandardView {
    // Демо-экран для тренинга: на одном примере показывает разницу между plain text и HTML rendering.

    private static final String SAMPLE_HTML = """
            <div>
                <h4>Safety briefing</h4>
                <p>Read the <b>PPE checklist</b> before signing.</p>
                <ul>
                    <li>Helmet</li>
                    <li>Gloves</li>
                    <li>Eye protection</li>
                </ul>
            </div>
            """;

    @ViewComponent
    private Span spanExample;
    @ViewComponent
    private JmixTextArea textAreaExample;
    @ViewComponent
    private Html dynamicHtmlExample;

    @Subscribe
    public void onInit(InitEvent event) {
        spanExample.setText(SAMPLE_HTML);
        textAreaExample.setValue(SAMPLE_HTML);

        // Html expects exactly one root tag and renders markup instead of showing the tags as text.
        dynamicHtmlExample.setHtmlContent(SAMPLE_HTML);
    }
}
