import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class BriefcaseButton extends JButton {

    public enum State { CLOSED, SELECTED, OPENING, OPEN }

    private State bState = State.CLOSED;
    private boolean hovered = false;
    private boolean peekHighlight = false;
    private Color flashColor = null;
    private String openingText = null;

    static final Color GOLD = new Color(212, 175, 55);

    public BriefcaseButton(String number) {
        super(number);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setBriefcaseState(State s) { this.bState = s; repaint(); }
    public void setHovered(boolean h)       { this.hovered = h; repaint(); }
    public void setPeekHighlight(boolean p) { this.peekHighlight = p; repaint(); }
    public void setFlashColor(Color c)      { this.flashColor = c; repaint(); }
    public void setOpeningText(String t)    { this.openingText = t; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();

        // ── Colours by state ─────────────────────────────────────
        Color bodyTop, bodyBot, borderCol, handleCol, latchCol, textCol;

        if (flashColor != null) {
            bodyTop = flashColor; bodyBot = flashColor.darker();
            borderCol = Color.WHITE; handleCol = Color.WHITE;
            latchCol = Color.WHITE; textCol = Color.WHITE;
        } else if (bState == State.OPEN) {
            bodyTop = new Color(22, 22, 28); bodyBot = new Color(14, 14, 18);
            borderCol = new Color(45, 45, 55); handleCol = new Color(35, 35, 42);
            latchCol = new Color(40, 40, 50); textCol = new Color(60, 60, 70);
        } else if (bState == State.SELECTED) {
            bodyTop = new Color(175, 130, 10); bodyBot = new Color(120, 85, 5);
            borderCol = new Color(255, 215, 60); handleCol = new Color(220, 180, 30);
            latchCol = new Color(240, 200, 50); textCol = new Color(25, 15, 0);
        } else if (bState == State.OPENING) {
            bodyTop = new Color(15, 15, 20); bodyBot = new Color(8, 8, 12);
            borderCol = GOLD; handleCol = GOLD;
            latchCol = GOLD; textCol = GOLD;
        } else if (peekHighlight) {
            bodyTop = new Color(90, 38, 140); bodyBot = new Color(60, 22, 95);
            borderCol = new Color(170, 80, 240); handleCol = new Color(140, 60, 210);
            latchCol = new Color(160, 70, 225); textCol = Color.WHITE;
        } else if (hovered) {
            bodyTop = new Color(45, 88, 178); bodyBot = new Color(28, 58, 128);
            borderCol = GOLD; handleCol = new Color(200, 165, 40);
            latchCol = new Color(220, 185, 50); textCol = Color.WHITE;
        } else {
            bodyTop = new Color(28, 58, 122); bodyBot = new Color(16, 36, 82);
            borderCol = new Color(70, 108, 195); handleCol = new Color(165, 132, 38);
            latchCol = new Color(185, 150, 45); textCol = Color.WHITE;
        }

        // ── Dimensions ──────────────────────────────────────────
        int pad    = 5;
        int bodyX  = pad;
        int bodyW  = w - pad * 2;
        int handleH = h / 7;
        int bodyY  = handleH + 4;
        int bodyH  = h - bodyY - pad;
        int arc    = 10;

        // ── Drop shadow ─────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRoundRect(bodyX + 3, bodyY + 3, bodyW, bodyH, arc, arc);

        // ── Body gradient ────────────────────────────────────────
        GradientPaint bodyGrad = new GradientPaint(bodyX, bodyY, bodyTop,
                bodyX, bodyY + bodyH, bodyBot);
        g2.setPaint(bodyGrad);
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, arc, arc);

        // ── Lid / body split ─────────────────────────────────────
        if (bState != State.OPEN) {
            int lidH = bodyH / 3;
            // lid slightly lighter gradient
            GradientPaint lidGrad = new GradientPaint(bodyX, bodyY,
                    bodyTop.brighter(), bodyX, bodyY + lidH, bodyTop);
            g2.setPaint(lidGrad);
            g2.fillRoundRect(bodyX, bodyY, bodyW, lidH + arc / 2, arc, arc);
            g2.setColor(bodyTop);
            g2.fillRect(bodyX, bodyY + lidH - 2, bodyW, arc);

            // divider line with highlight
            g2.setColor(borderCol.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(bodyX + arc / 2, bodyY + lidH, bodyX + bodyW - arc / 2, bodyY + lidH);
            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(bodyX + arc / 2, bodyY + lidH + 1, bodyX + bodyW - arc / 2, bodyY + lidH + 1);

            // ── Latch ────────────────────────────────────────────
            int lW = 20, lH = 12;
            int lX = (w - lW) / 2, lY = bodyY + lidH - lH / 2;
            GradientPaint latchGrad = new GradientPaint(lX, lY, latchCol.brighter(), lX, lY + lH, latchCol.darker());
            g2.setPaint(latchGrad);
            g2.fillRoundRect(lX, lY, lW, lH, 4, 4);
            g2.setColor(new Color(0, 0, 0, 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(lX, lY, lW, lH, 4, 4);
            // latch keyhole
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillOval(lX + lW / 2 - 2, lY + lH / 2 - 2, 5, 5);

            // ── Side rivets ─────────────────────────────────────
            int rivetY = bodyY + bodyH - 10;
            for (int rx : new int[]{bodyX + 8, bodyX + bodyW - 12}) {
                g2.setColor(borderCol.darker());
                g2.fillOval(rx, rivetY, 5, 5);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(rx + 1, rivetY + 1, 2, 2);
            }
        }

        // ── Body border ─────────────────────────────────────────
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(bState == State.OPEN ? 1.5f : 2f));
        g2.drawRoundRect(bodyX, bodyY, bodyW, bodyH, arc, arc);

        // Inner highlight
        g2.setColor(new Color(255, 255, 255, 18));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bodyX + 2, bodyY + 2, bodyW - 4, bodyH / 3, arc - 2, arc - 2);

        // ── Handle ──────────────────────────────────────────────
        int hW = bodyW / 3;
        int hX = bodyX + (bodyW - hW) / 2;

        // handle shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(hX + 1, 3, hW, handleH * 2, 0, 180);

        // handle gradient
        GradientPaint handleGrad = new GradientPaint(hX, 2, handleCol.brighter(), hX, handleH * 2, handleCol.darker());
        g2.setPaint(handleGrad);
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(hX, 2, hW, handleH * 2, 0, 180);

        // handle attachment nubs
        g2.setColor(borderCol);
        g2.setStroke(new BasicStroke(2f));
        g2.fillOval(hX - 2, bodyY - 2, 6, 6);
        g2.fillOval(hX + hW - 4, bodyY - 2, 6, 6);

        // ── Text / label ─────────────────────────────────────────
        String txt;
        Font font;
        if (bState == State.OPENING && openingText != null) {
            txt = openingText; font = new Font("Arial", Font.BOLD, 11);
        } else if (bState == State.OPEN) {
            txt = "✕"; font = new Font("Arial", Font.BOLD, 15);
        } else {
            txt = getText(); font = new Font("Georgia", Font.BOLD, 17);
        }

        // subtle text shadow
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(txt)) / 2;
        int ty = bodyY + (bodyH * 2 / 3) + fm.getAscent() / 2;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(txt, tx + 1, ty + 1);
        g2.setColor(textCol);
        g2.drawString(txt, tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() { return new Dimension(105, 86); }
}