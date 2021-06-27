package main;

import main.clone.ClonePair;
import main.clone.EnumCloneType;
import main.matching.IMatcher;
import main.matching.IMethodMatching;
import main.matching.MatchingException;
import main.method.Method;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.List;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 * GUI that allows for browsing clones.
 */
public class Application
{
    private final JFrame m_window;

    private final JScrollPane m_method1Scroll;
    private final JTextPane m_method1Text;

    private final JScrollPane m_method2Scroll;
    private final JTextPane m_method2Text;

    private final JLabel m_cloneCountLabel;
    private final JLabel m_manualCloneTypeLabel;
    private final JLabel m_predictedCloneTypeLabel;

    private int m_currentCloneIdx = 0;
    private final List<ClonePair> m_clones;

    private final IMatcher m_matcher;


    /**
     * Constructor.
     *
     * @param window_title The title of the window.
     * @param clones The list of clones that will be displayed.
     * @param matcher The matcher that produces matchings. Each matching will be used to display the method source.
     */
    public Application(String window_title, List<ClonePair> clones, IMatcher matcher) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException
    {
        m_clones = clones;

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // initialise window
        m_window = new JFrame(window_title);
        m_window.setLocation(250,250);
        m_window.setSize(1000, 750);
        m_window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        m_window.setLayout(new BorderLayout());

        // create text areas
        m_method1Text = new JTextPane();
        m_method1Text.setEditable(false);
        JPanel method1_nowrap_panel = new JPanel(new BorderLayout());
        method1_nowrap_panel.add(m_method1Text);
        m_method1Scroll = new JScrollPane(method1_nowrap_panel);
        m_method1Scroll.getVerticalScrollBar().setUnitIncrement(30);
        m_method1Scroll.getHorizontalScrollBar().setUnitIncrement(30);

        m_method2Text = new JTextPane();
        m_method2Text.setEditable(false);
        JPanel method2_nowrap_panel = new JPanel(new BorderLayout());
        method2_nowrap_panel.add(m_method2Text);
        m_method2Scroll = new JScrollPane(method2_nowrap_panel);
        m_method2Scroll.getVerticalScrollBar().setUnitIncrement(30);
        m_method2Scroll.getHorizontalScrollBar().setUnitIncrement(30);

        // put text areas side by side
        JSplitPane code_panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, m_method1Scroll, m_method2Scroll);
        code_panel.setResizeWeight(0.5);
        // NOTE: CENTER layout makes it take up all the empty space
        m_window.getContentPane().add(BorderLayout.CENTER, code_panel);

        JPanel bottom_panel = new JPanel(new BorderLayout());
        m_window.getContentPane().add(BorderLayout.SOUTH, bottom_panel);

        // add previous and next buttons
        JPanel button_panel = new JPanel();
        bottom_panel.add(BorderLayout.NORTH, button_panel);

        // skip to begin
        JButton first_button = new JButton("Begin");
        button_panel.add(first_button);
        first_button.addActionListener(e -> {
            m_currentCloneIdx = 0;
            M_loadCurrentClone();
        });

        // reverse 1000
        JButton fast_skip_prev_button = new JButton("<<<");
        button_panel.add(fast_skip_prev_button);
        fast_skip_prev_button.addActionListener(e -> M_loadPrev(1000));

        // reverse 100
        JButton skip_prev_button = new JButton("<<");
        button_panel.add(skip_prev_button);
        skip_prev_button.addActionListener(e -> M_loadPrev(100));

        // reverse 1
        JButton prev_button = new JButton("Prev");
        button_panel.add(prev_button);
        prev_button.addActionListener(e -> M_loadPrev(1));

        // forward 1
        JButton next_button = new JButton("Next");
        button_panel.add(next_button);
        next_button.addActionListener(e -> M_loadNext(1));

        // forward 100
        JButton skip_next_button = new JButton(">>");
        button_panel.add(skip_next_button);
        skip_next_button.addActionListener(e -> M_loadNext(100));

        // forward 1000
        JButton fast_skip_next_button = new JButton(">>>");
        button_panel.add(fast_skip_next_button);
        fast_skip_next_button.addActionListener(e -> M_loadNext(1000));

        // skip to last
        JButton last_button = new JButton("End");
        button_panel.add(last_button);
        last_button.addActionListener(e -> {
            m_currentCloneIdx = m_clones.size() - 1;
            M_loadCurrentClone();
        });


        JPanel label_panel = new JPanel();
        bottom_panel.add(BorderLayout.SOUTH, label_panel);

        // label that displays how many clone pairs there are how the currently viewed clone pair index
        m_cloneCountLabel = new JLabel();
        label_panel.add(m_cloneCountLabel);

        // label that displays the manually assigned class
        m_manualCloneTypeLabel = new JLabel();
        label_panel.add(m_manualCloneTypeLabel);

        // label that displays the predicted clone type
        m_predictedCloneTypeLabel = new JLabel();
        label_panel.add(m_predictedCloneTypeLabel);

        // set matcher
        m_matcher = matcher;

        // load clones and display first clone
        m_currentCloneIdx = 0;
        M_loadCurrentClone();

    }

    /**
     * Set the title of the specified scroll pane.
     */
    private void M_setMethodTitle(JScrollPane scroll_pane, String title)
    {
        scroll_pane.setBorder(BorderFactory.createTitledBorder(title));
    }

    /**
     * Add the specified text to the specified {@link JTextPane} with the specified background color.
     */
    private void M_appendToPane(JTextPane target, String text, Color bg_color)
    {
        target.setEditable(true);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, bg_color);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = target.getDocument().getLength();
        target.setCaretPosition(len);
        target.setCharacterAttributes(aset, false);
        target.replaceSelection(text);

        target.setEditable(false);
    }

    /**
     * Take step forward.
     *
     * @param delta The number of steps to take forward.
     */
    private void M_loadNext(int delta)
    {
        // move to next index
        m_currentCloneIdx += delta;

        // loop to front
        if(m_currentCloneIdx >= m_clones.size()) {
            m_currentCloneIdx = m_clones.size() - 1;
        }

        M_loadCurrentClone();
    }

    /**
     * Take step backward.
     *
     * @param delta The number of steps to take backward.
     */
    private void M_loadPrev(int delta)
    {
        // move to next index
        m_currentCloneIdx -= delta;

        // loop to front
        if(m_currentCloneIdx < 0) {
            m_currentCloneIdx = 0;
        }

        M_loadCurrentClone();
    }

    /**
     * Load the currently selected clone into the GUI.
     */
    private void M_loadCurrentClone()
    {
        // remove current text
        m_method1Text.setText("");
        m_method2Text.setText("");

        // retrieve the current clone
        ClonePair current_clone = m_clones.get(m_currentCloneIdx);

        // set labels, titles, etc
        M_setMethodTitle(m_method1Scroll, String.format(
                "%s:%d->%d",
                current_clone.getMethod1().getPath(),
                current_clone.getMethod1().getBegin(),
                current_clone.getMethod1().getEnd()
        ));

        M_setMethodTitle(m_method2Scroll, String.format(
                "%s:%d->%d",
                current_clone.getMethod2().getPath(),
                current_clone.getMethod2().getBegin(),
                current_clone.getMethod2().getEnd()
        ));

        // update some labels
        m_cloneCountLabel.setText((m_currentCloneIdx+1) + "/" + m_clones.size());
        m_manualCloneTypeLabel.setText("[truth=" + current_clone.getManualClassification().name() + "]");

        try {
            // determine the matching between the two methods
            IMethodMatching matching = m_matcher.match(current_clone.getMethod1(), current_clone.getMethod2());

            // display the matched source codes of the methods
            matching.writeMatchedMethod1((s, c) -> M_appendToPane(m_method1Text, s, c));
            matching.writeMatchedMethod2((s, c) -> M_appendToPane(m_method2Text, s, c));

            // get classification
            EnumCloneType type_prediction = matching.classify();

            // display the predicted class
            if(type_prediction != null) {
                m_predictedCloneTypeLabel.setText("[predicted=" + matching.classify().name() + "]");
            } else {
                m_predictedCloneTypeLabel.setText("[predicted=null]");
            }
        } catch (MatchingException e) {
            M_appendToPane(m_method1Text, "ERROR: Cannot load clone!", Color.RED);
            M_appendToPane(m_method2Text, "ERROR: Cannot load clone!", Color.RED);

            // print error
            Main.log("Error when matching pair:");
            Main.log("\tIdx: %d", m_currentCloneIdx);
            Method method1 = current_clone.getMethod1();
            Main.log("\tSource 1: (%s, %d, %d)", method1.getPath().getPath(), method1.getBegin(), method1.getEnd());
            Method method2 = current_clone.getMethod2();
            Main.log("\tSource 2: (%s, %d, %d)", method2.getPath().getPath(), method2.getBegin(), method2.getEnd());
            e.printStackTrace();
        }
    }

    /**
     * Run the application.
     */
    public void run()
    {
        m_window.setVisible(true);
    }
}
