package ethan.hoenn.rnbrules.utils.enums;

import net.minecraft.util.ResourceLocation;

public enum BoardType {
	WOOD("location_board.png"),
	WATER("location_water.png"),
	CAVE("location_cave.png");

	private final String textureFileName;
	private final ResourceLocation texture;

	BoardType(String textureFileName) {
		this.textureFileName = textureFileName;
		this.texture = new ResourceLocation("rnbrules", "textures/gui/" + textureFileName);
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	public String getTextureFileName() {
		return textureFileName;
	}
}
