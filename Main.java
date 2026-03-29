import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Main {

    static final Color BG         = new Color(8, 8, 15);
    static final Color GOLD       = new Color(212, 175, 55);
    static final Color GREEN      = new Color(0, 180, 80);
    static final Color RED        = new Color(200, 40, 40);
    static final Color HIGH_COLOR = new Color(80, 160, 255);
    static final Color LOW_COLOR  = new Color(210, 210, 210);

    static JLabel statusLabel;
    static JLabel evLabel;
    static JLabel roundLabel;
    static JLabel yourCaseLabel;
    static JLabel prevOfferLabel;
    static JButton peekBtn;

    public static void main(String[] args) {
        showStartScreen();

        GameState game = new GameState();
        JFrame window = new JFrame("Deal or No Deal");
        window.setSize(1150, 760);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setBackground(BG);
        window.setLayout(new BorderLayout(0, 0));

        // TOP BAR
        JPanel topPanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 40),
                        getWidth(), 0, new Color(40, 30, 5));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, GOLD),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JLabel title = new JLabel("DEAL  OR  NO  DEAL", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 36));
        title.setForeground(GOLD);

        roundLabel = new JLabel("Round 1  |  Open 6 cases", SwingConstants.RIGHT);
        roundLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roundLabel.setForeground(new Color(180, 150, 60));

        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(roundLabel, BorderLayout.EAST);
        window.add(topPanel, BorderLayout.NORTH);

        // MONEY BOARDS
        long[] lowAmounts  = {1, 100, 1000, 5000, 10000, 25000, 50000,
                              75000, 100000, 200000, 300000, 400000, 500000};
        long[] highAmounts = {750000, 1000000, 5000000, 10000000, 25000000,
                              50000000, 75000000, 100000000, 200000000,
                              300000000, 500000000, 750000000, 1000000000};

        JPanel leftPanel  = buildMoneyPanel();
        JPanel rightPanel = buildMoneyPanel();
        JLabel[] leftLabels  = new JLabel[13];
        JLabel[] rightLabels = new JLabel[13];

        for (int i = 0; i < lowAmounts.length; i++) {
            leftLabels[i] = buildMoneyLabel(formatMoney(lowAmounts[i]), LOW_COLOR);
            leftPanel.add(leftLabels[i]);
        }
        for (int i = 0; i < highAmounts.length; i++) {
            rightLabels[i] = buildMoneyLabel(formatMoney(highAmounts[i]), HIGH_COLOR);
            rightPanel.add(rightLabels[i]);
        }

        // BRIEFCASE GRID
        JPanel centerPanel = new JPanel(new GridLayout(4, 7, 12, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        for (int i = 1; i <= 26; i++) {
            BriefcaseButton btn = new BriefcaseButton("" + i);
            int caseNum = i;

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (btn.isEnabled()) {
                        btn.setHovered(true);
                        btn.setPeekHighlight(game.peekMode);
                    }
                }
                public void mouseExited(MouseEvent e) {
                    btn.setHovered(false);
                    btn.setPeekHighlight(false);
                }
            });

            btn.addActionListener(e -> {
                if (!game.playerCaseSelected) {
                    game.playerCase = caseNum;
                    game.playerCaseSelected = true;
                    btn.setBriefcaseState(BriefcaseButton.State.SELECTED);
                    btn.setEnabled(false);
                    yourCaseLabel.setText("  \uD83D\uDCBC  Your Case: #" + caseNum);
                    int toOpen = game.casesPerRound[game.round - 1];
                    statusLabel.setText("Open " + toOpen + " cases this round");
                    roundLabel.setText("Round 1  |  Open " + toOpen + " cases");
                    peekBtn.setEnabled(true);

                } else if (game.peekMode) {
                    game.peekMode = false;
                    game.peekUsed = true;
                    peekBtn.setEnabled(false);
                    peekBtn.setText("Peek Used");
                    long val = game.cases[caseNum - 1].value;
                    JOptionPane.showMessageDialog(window,
                            "Case #" + caseNum + " contains " + formatMoney(val) + "!\n(Case is NOT eliminated)",
                            "Peek Result \uD83D\uDD0D", JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText("Peek used! Now open a case.");

                } else {
                    if (!btn.isEnabled() || game.cases[caseNum - 1].isOpen) return;

                    game.cases[caseNum - 1].isOpen = true;
                    long openedValue = game.cases[caseNum - 1].value;

                    animateOpenCase(btn, openedValue, window);

                    for (int j = 0; j < lowAmounts.length; j++) {
                        if (lowAmounts[j] == openedValue)
                            strikeLabel(leftLabels[j], formatMoney(lowAmounts[j]));
                    }
                    for (int j = 0; j < highAmounts.length; j++) {
                        if (highAmounts[j] == openedValue)
                            strikeLabel(rightLabels[j], formatMoney(highAmounts[j]));
                    }

                    evLabel.setText("EV: " + formatMoney(game.calculateEV()));
                    game.casesOpenedThisRound++;

                    // Count remaining
                    int remaining = 0;
                    Briefcase lastCase = null;
                    for (Briefcase b : game.cases) {
                        if (!b.isOpen && b.id != game.playerCase) {
                            remaining++;
                            lastCase = b;
                        }
                    }

                    // Disable peek if <=3 remaining
                    if (!game.peekUsed && remaining <= 3) {
                        peekBtn.setEnabled(false);
                        peekBtn.setText("\uD83D\uDD0D N/A (<3 left)");
                    }

                    // End game
                    if (remaining == 1) {
                        final Briefcase finalLast = lastCase;
                        Timer t = new Timer(800, ev -> {
                            long myValue = game.cases[game.playerCase - 1].value;
                            int choice = JOptionPane.showConfirmDialog(window,
                                    "One case remains!\nSwap your case #" + game.playerCase + "?",
                                    "Final Decision", JOptionPane.YES_NO_OPTION);
                            if (choice == JOptionPane.YES_OPTION) showWinDialog(window, finalLast.value, true);
                            else showWinDialog(window, myValue, false);
                            System.exit(0);
                        });
                        t.setRepeats(false);
                        t.start();
                        return;
                    }

                    // Banker offer
                    if (game.round <= game.casesPerRound.length &&
                            game.casesOpenedThisRound == game.casesPerRound[game.round - 1]) {

                        game.casesOpenedThisRound = 0;
                        game.round++;
                        long offer = calculateOffer(game);
                        game.previousOffers.add(offer);

                        String prevText = game.previousOffers.size() > 1
                                ? "Prev: " + formatMoney(game.previousOffers.get(game.previousOffers.size() - 2))
                                : "";
                        prevOfferLabel.setText(prevText);

                        Timer delay = new Timer(400, ev -> {
                            boolean deal = showBankerDialog(window, offer, game);
                            if (deal) { showWinDialog(window, offer, false); System.exit(0); }
                            if (game.round <= game.casesPerRound.length) {
                                int toOpen = game.casesPerRound[game.round - 1];
                                roundLabel.setText("Round " + game.round + "  |  Open " + toOpen + " cases");
                                statusLabel.setText("No deal! Open " + toOpen + " more cases.");
                            }
                        });
                        delay.setRepeats(false);
                        delay.start();

                    } else {
                        int needed = game.round <= game.casesPerRound.length
                                ? game.casesPerRound[game.round - 1] : 1;
                        statusLabel.setText("Open " + (needed - game.casesOpenedThisRound) + " more cases");
                    }
                }
            });
            centerPanel.add(btn);
        }

        window.add(leftPanel, BorderLayout.WEST);
        window.add(centerPanel, BorderLayout.CENTER);
        window.add(rightPanel, BorderLayout.EAST);

        // BOTTOM BAR
        JPanel bottomPanel = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(12, 12, 22));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, GOLD),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        yourCaseLabel = new JLabel("  Pick your briefcase!");
        yourCaseLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        yourCaseLabel.setForeground(GOLD);

        prevOfferLabel = new JLabel("");
        prevOfferLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        prevOfferLabel.setForeground(new Color(140, 140, 140));

        peekBtn = new JButton("\uD83D\uDD0D  Peek (1x)");
        peekBtn.setFont(new Font("Arial", Font.BOLD, 12));
        peekBtn.setBackground(new Color(80, 40, 120));
        peekBtn.setForeground(Color.WHITE);
        peekBtn.setFocusPainted(false);
        peekBtn.setEnabled(false);
        peekBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        peekBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        peekBtn.addActionListener(e -> {
            if (game.peekUsed) return;
            int rem = 0;
            for (Briefcase b : game.cases)
                if (!b.isOpen && b.id != game.playerCase) rem++;
            if (rem <= 3) {
                JOptionPane.showMessageDialog(window,
                        "Peek can't be used when 3 or fewer cases remain!",
                        "Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            game.peekMode = true;
            statusLabel.setText("Click any unopened case to peek inside!");
            peekBtn.setBackground(new Color(120, 60, 180));
        });

        statusLabel = new JLabel("Choose a briefcase to begin", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 15));
        statusLabel.setForeground(Color.WHITE);

        evLabel = new JLabel("EV: " + formatMoney(game.calculateEV()), SwingConstants.RIGHT);
        evLabel.setFont(new Font("Arial", Font.BOLD, 14));
        evLabel.setForeground(new Color(80, 200, 120));

        JPanel bLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bLeft.setOpaque(false);
        bLeft.add(yourCaseLabel);
        bLeft.add(prevOfferLabel);
        bLeft.add(peekBtn);

        bottomPanel.add(bLeft, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(evLabel, BorderLayout.EAST);

        window.add(bottomPanel, BorderLayout.SOUTH);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    // START SCREEN
    static void showStartScreen() {
        JDialog d = new JDialog();
        d.setUndecorated(true);
        d.setSize(660, 430);
        d.setLocationRelativeTo(null);
        d.setModal(true);

        JPanel panel = new JPanel(new BorderLayout(0, 16)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(5, 5, 15),
                        getWidth(), getHeight(), new Color(25, 20, 5));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(35, 50, 35, 50));

        JLabel deal = new JLabel("DEAL", SwingConstants.CENTER);
        deal.setFont(new Font("Georgia", Font.BOLD, 76));
        deal.setForeground(GOLD);

        JLabel or = new JLabel("OR", SwingConstants.CENTER);
        or.setFont(new Font("Georgia", Font.ITALIC, 30));
        or.setForeground(new Color(200, 170, 60));

        JLabel noDeal = new JLabel("NO DEAL", SwingConstants.CENTER);
        noDeal.setFont(new Font("Georgia", Font.BOLD, 58));
        noDeal.setForeground(Color.WHITE);

        JLabel sub = new JLabel("India Edition  •  26 Briefcases  •  ₹100 Cr Jackpot", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.ITALIC, 13));
        sub.setForeground(new Color(140, 140, 140));

        JPanel titlePanel = new JPanel(new GridLayout(4, 1, 0, 2));
        titlePanel.setOpaque(false);
        titlePanel.add(deal);
        titlePanel.add(or);
        titlePanel.add(noDeal);
        titlePanel.add(sub);

        JButton startBtn = makeStartBtn("▶   START GAME", GREEN);
        JButton rulesBtn = makeStartBtn("?   HOW TO PLAY", new Color(55, 55, 100));

        startBtn.addActionListener(e -> d.dispose());
        rulesBtn.addActionListener(e -> showRules(d));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        btnPanel.add(startBtn);
        btnPanel.add(rulesBtn);

        panel.add(titlePanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        d.add(panel);
        d.setVisible(true);
    }

    static void showRules(JDialog parent) {
        JDialog rules = new JDialog(parent, "Rules", true);
        rules.setUndecorated(true);
        rules.setSize(520, 390);
        rules.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(0, 12)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(10, 10, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 2),
                BorderFactory.createEmptyBorder(22, 28, 22, 28)));

        JLabel rTitle = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        rTitle.setFont(new Font("Georgia", Font.BOLD, 22));
        rTitle.setForeground(GOLD);

        JLabel rulesLabel = new JLabel("<html><div style='color:white;font-size:13px;line-height:1.7'>" +
                "1. <b>Pick your briefcase</b> — it turns gold. Keep it till the end.<br><br>" +
                "2. <b>Open other cases</b> to eliminate amounts from the board.<br><br>" +
                "3. <b>Banker calls</b> after each round with a cash offer. Deal or No Deal?<br><br>" +
                "4. <b>Counter-offer</b> the banker — but rejection means the original offer is gone!<br><br>" +
                "5. <b>Peek lifeline</b> (once) — see inside a case without eliminating it.<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;Only usable when more than 3 cases remain.<br><br>" +
                "6. At the end, <b>swap or keep</b> your case to reveal your final prize." +
                "</div></html>");

        JButton closeBtn = makeDialogBtn("Got it!", new Color(50, 90, 50));
        closeBtn.addActionListener(e -> rules.dispose());

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnP.setOpaque(false);
        btnP.add(closeBtn);

        panel.add(rTitle, BorderLayout.NORTH);
        panel.add(rulesLabel, BorderLayout.CENTER);
        panel.add(btnP, BorderLayout.SOUTH);
        rules.add(panel);
        rules.setVisible(true);
    }

    static JButton makeStartBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ANIMATIONS
    static void animateOpenCase(BriefcaseButton btn, long value, JFrame window) {
        btn.setEnabled(false);
        boolean high = value >= 1000000;
        Color fc = high ? new Color(255, 200, 0) : new Color(200, 40, 40);
        Color[] frames = {fc, Color.WHITE, fc, Color.WHITE, fc};
        Timer flashTimer = new Timer(120, null);
        int[] step = {0};
        flashTimer.addActionListener(e -> {
            if (step[0] < frames.length) {
                btn.setFlashColor(frames[step[0]++]);
            } else {
                flashTimer.stop();
                btn.setFlashColor(null);
                btn.setOpeningText(formatMoney(value));
                btn.setBriefcaseState(BriefcaseButton.State.OPENING);
                showRevealPopup(window, value, high);
                Timer closeTimer = new Timer(1600, ev -> {
                    btn.setOpeningText(null);
                    btn.setBriefcaseState(BriefcaseButton.State.OPEN);
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
        flashTimer.start();
    }

    static void showRevealPopup(JFrame parent, long value, boolean high) {
        JDialog popup = new JDialog(parent, false);
        popup.setUndecorated(true);
        popup.setSize(300, 95);
        popup.setLocationRelativeTo(parent);
        Point loc = popup.getLocation();
        popup.setLocation(loc.x, loc.y - 110);

        Color borderCol = high ? new Color(200, 170, 0) : new Color(180, 40, 40);

        JPanel panel = new JPanel(new BorderLayout(0, 4)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(12, 12, 22));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderCol, 2),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JLabel msg = new JLabel("Revealed: " + formatMoney(value), SwingConstants.CENTER);
        msg.setFont(new Font("Georgia", Font.BOLD, 20));
        msg.setForeground(high ? new Color(255, 215, 0) : new Color(255, 100, 100));

        JLabel sub = new JLabel(high ? "\uD83D\uDD25 Big value eliminated!" : "Low value eliminated.", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.ITALIC, 12));
        sub.setForeground(new Color(140, 140, 140));

        panel.add(msg, BorderLayout.CENTER);
        panel.add(sub, BorderLayout.SOUTH);
        popup.add(panel);
        popup.setVisible(true);

        Timer t = new Timer(1900, e -> popup.dispose());
        t.setRepeats(false);
        t.start();
    }

    static void strikeLabel(JLabel lbl, String text) {
        lbl.setText("  ✕  " + text);
        lbl.setForeground(RED);
        lbl.setBackground(new Color(18, 18, 18));
        Timer t = new Timer(500, e -> lbl.setForeground(new Color(70, 70, 70)));
        t.setRepeats(false);
        t.start();
    }

    // BANKER DIALOG
    static boolean showBankerDialog(JFrame parent, long offer, GameState game) {
        JDialog dialog = new JDialog(parent, "The Banker", true);
        dialog.setUndecorated(true);
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(parent);

        long ev = game.calculateEV();
        String evString = ev > 0
                ? "EV: " + formatMoney(ev) + "   |   Offer is " + Math.round(offer * 100.0 / ev) + "% of EV"
                : "EV: --";

        JPanel panel = new JPanel(new BorderLayout(0, 0)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 20),
                        0, getHeight(), new Color(25, 20, 5));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createLineBorder(GOLD, 2));

        JLabel bankerTitle = new JLabel("THE BANKER CALLS", SwingConstants.CENTER);
        bankerTitle.setFont(new Font("Georgia", Font.BOLD, 16));
        bankerTitle.setForeground(new Color(160, 130, 40));
        bankerTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JLabel offerLabel = new JLabel(formatMoney(offer), SwingConstants.CENTER);
        offerLabel.setFont(new Font("Georgia", Font.BOLD, 48));
        offerLabel.setForeground(Color.WHITE);

        Timer pulse = new Timer(600, null);
        boolean[] bright = {true};
        pulse.addActionListener(e -> {
            offerLabel.setForeground(bright[0] ? Color.WHITE : GOLD);
            bright[0] = !bright[0];
        });
        pulse.start();

        JLabel evLine = new JLabel(evString, SwingConstants.CENTER);
        evLine.setFont(new Font("Arial", Font.PLAIN, 12));
        evLine.setForeground(new Color(140, 140, 140));

        JPanel centerInfo = new JPanel(new GridLayout(3, 1, 0, 4));
        centerInfo.setOpaque(false);
        centerInfo.add(bankerTitle);
        centerInfo.add(offerLabel);
        centerInfo.add(evLine);

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 20, 25));

        JButton dealBtn    = makeDialogBtn("DEAL!", GREEN);
        JButton counterBtn = makeDialogBtn("Counter", new Color(160, 120, 0));
        JButton noDealBtn  = makeDialogBtn("NO DEAL!", RED);

        boolean[] result = {false};

        dealBtn.addActionListener(e    -> { pulse.stop(); result[0] = true; dialog.dispose(); });
        noDealBtn.addActionListener(e  -> { pulse.stop(); dialog.dispose(); });

        counterBtn.addActionListener(e -> {
            pulse.stop();
            dialog.dispose();
            String input = JOptionPane.showInputDialog(parent,
                    "Enter your counter-offer (numbers only):",
                    "Counter Offer", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    long counter = Long.parseLong(input.trim().replaceAll("[^0-9]", ""));
                    long ev2 = game.calculateEV();
                    int tolerance = new Random().nextInt(30) + 1;
                    long maxAccept = (long)(offer * (1 + tolerance / 100.0));
                    long bankerLimit = Math.min(maxAccept, ev2);

                    if (counter <= bankerLimit) {
                        int confirm = JOptionPane.showConfirmDialog(parent,
                                "Banker accepts " + formatMoney(counter) + "!\nTake the deal?",
                                "Counter Accepted!", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            showWinDialog(parent, counter, false);
                            System.exit(0);
                        }
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                "Banker rejects " + formatMoney(counter) + "!\nOriginal offer also withdrawn.",
                                "Counter Rejected!", JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(dealBtn);
        btnPanel.add(counterBtn);
        btnPanel.add(noDealBtn);

        panel.add(centerInfo, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
        return result[0];
    }

    static JButton makeDialogBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    static void showWinDialog(JFrame parent, long amount, boolean swapped) {
        String msg = (swapped ? "You swapped!\n" : "You kept your case!\n")
                + "You won: " + formatMoney(amount) + "!";
        JOptionPane.showMessageDialog(parent, msg, "CONGRATULATIONS!", JOptionPane.PLAIN_MESSAGE);
    }

    static JPanel buildMoneyPanel() {
        JPanel p = new JPanel(new GridLayout(13, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        p.setPreferredSize(new Dimension(140, 0));
        return p;
    }

    static JLabel buildMoneyLabel(String text, Color color) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(color);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(20, 20, 30));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 70), 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        return lbl;
    }

    static String formatMoney(long amount) {
        if (amount >= 10000000) return "₹" + (amount / 10000000) + " Cr";
        if (amount >= 100000)   return "₹" + (amount / 100000) + " L";
        if (amount >= 1000)     return "₹" + (amount / 1000) + " K";
        return "₹" + amount;
    }

    static long calculateOffer(GameState game) {
        long sum = 0; int remaining = 0;
        for (Briefcase b : game.cases) {
            if (!b.isOpen && b.id != game.playerCase) { sum += b.value; remaining++; }
        }
        if (remaining == 0) return 0;
        long avg = sum / remaining;
        double factor = 0.45 + (game.round * 0.06);
        return (long)(avg * Math.min(factor, 0.95));
    }
}