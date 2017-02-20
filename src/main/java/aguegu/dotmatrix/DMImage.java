package aguegu.dotmatrix;

import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class DMImage extends BufferedImage {
	private static int blockWidth = 13;
	private static Color backgroundColor = Color.lightGray;
	private static Color onColor = Color.white;
	private static Color offColor = Color.gray;
	private static Color highlightColor = new Color(211, 54, 54);
	private static BasicStroke bs;

	private Graphics2D g2d;
	private boolean[] dot;
	private boolean[] highlight;

	public DMImage() {
		super(blockWidth * 8, blockWidth * 8, BufferedImage.TYPE_INT_ARGB);

		dot = new boolean[64];
		highlight = new boolean[64];

		g2d = this.createGraphics();
		g2d.setBackground(backgroundColor);
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

		g2d.setStroke(new BasicStroke(2));

		this.update();
	}

	public void update() {
		for (int r = 0, y = 0; r < 8; r++, y += blockWidth) {
			for (int c = 0, x = 0; c < 8; c++, x += blockWidth) {
				g2d.setColor(dot[r * 8 + c] ? onColor : offColor);
				g2d.fillRect(x, y, blockWidth - 1, blockWidth - 1);
				if (highlight[r * 8 + c]) {
					g2d.setColor(highlightColor);
					g2d.drawRect(x + 1, y + 1, blockWidth - 3, blockWidth - 3);
				}
			}
		}
	}

	public void setHighlight(int index, boolean value) {
		highlight[index] = value;
	}

	public void setDot(int index, boolean value) {
		try {
			dot[index] = value;
		} catch (ArrayIndexOutOfBoundsException ex) {
		}
	}

	public static int getBlockWidth() {
		return blockWidth;
	}
}
