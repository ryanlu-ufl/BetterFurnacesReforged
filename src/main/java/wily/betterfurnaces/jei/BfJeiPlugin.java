

package wily.betterfurnaces.jei;


import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import wily.betterfurnaces.BetterFurnacesReforged;
import wily.betterfurnaces.Config;
import wily.betterfurnaces.gui.*;
import wily.betterfurnaces.init.Registration;
import wily.betterfurnaces.items.ItemUpgradeTier;
import wily.betterfurnaces.recipes.CobblestoneGeneratorRecipes;
import wily.betterfurnaces.util.FluidRenderUtil;
import wily.betterfurnaces.util.GuiUtil;
import wily.betterfurnaces.util.RecipeUtil;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class BfJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(BetterFurnacesReforged.MOD_ID, "_plugin");
	}
	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		if (Config.enableJeiPlugin.get() && Config.enableJeiCatalysts.get()) {
			Block[] stack = {Registration.IRON_FURNACE.get(), Registration.GOLD_FURNACE.get(), Registration.DIAMOND_FURNACE.get(), Registration.NETHERHOT_FURNACE.get(), Registration.EXTREME_FURNACE.get(), Registration.EXTREME_FORGE.get()};

			for (Block i : stack) {
				ItemStack smelting = new ItemStack(i);
				registry.addRecipeCatalyst(smelting, RecipeTypes.SMELTING);

				registry.addRecipeCatalyst(smelting, RecipeTypes.FUELING);

				ItemStack blasting = smelting.copy();
				blasting.getOrCreateTag().putInt("type", 1);
				registry.addRecipeCatalyst(blasting, RecipeTypes.BLASTING);

				ItemStack smoking = smelting.copy();
				smoking.getOrCreateTag().putInt("type", 2);
				registry.addRecipeCatalyst(smoking, RecipeTypes.SMOKING);

			}

			registry.addRecipeCatalyst(new ItemStack(Registration.COBBLESTONE_GENERATOR.get()), Registration.ROCK_GENERATING_JEI);

		}
	}

	private void addDescription(IRecipeRegistration registry, ItemStack itemDefinition,
								Component... message) {
		registry.addIngredientInfo(itemDefinition, VanillaTypes.ITEM_STACK, message);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new CobblestoneGeneratorCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		Level world = Minecraft.getInstance().level;
		RecipeManager recipeManager = world.getRecipeManager();
		registration.addRecipes(Registration.ROCK_GENERATING_JEI, CobblestoneGeneratorRecipes.getRecipes(CobblestoneGeneratorRecipes.TYPE));
		ItemUpgradeTier[] up = {Registration.IRON_UPGRADE.get(), Registration.GOLD_UPGRADE.get(), Registration.DIAMOND_UPGRADE.get(), Registration.NETHERHOT_UPGRADE.get(), Registration.EXTREME_UPGRADE.get()};
		for (ItemUpgradeTier i : up)
			addDescription(registration, new ItemStack(i), new TextComponent(I18n.get("tooltip." + BetterFurnacesReforged.MOD_ID + ".upgrade.tier", i.from.getName().getString(), i.to.getName().getString())));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registry) {
		if (Config.enableJeiPlugin.get() && Config.enableJeiClickArea.get()) {
			registry.addRecipeClickArea(BlockIronFurnaceScreen.class, 79, 35, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockGoldFurnaceScreen.class, 79, 35, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockDiamondFurnaceScreen.class, 79, 35, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockExtremeForgeScreen.class, 80, 80, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockNetherhotFurnaceScreen.class, 79, 35, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockExtremeFurnaceScreen.class, 79, 35, 24, 17, RecipeTypes.SMELTING, RecipeTypes.FUELING);
			registry.addRecipeClickArea(BlockCobblestoneGeneratorScreen.class, 58, 44, 17, 12, Registration.ROCK_GENERATING_JEI);
			registry.addRecipeClickArea(BlockCobblestoneGeneratorScreen.class, 101, 44, 17, 12, Registration.ROCK_GENERATING_JEI);
		}
	}

	public static class CobblestoneGeneratorCategory implements IRecipeCategory<CobblestoneGeneratorRecipes> {
		private static ResourceLocation Uid = new ResourceLocation(BetterFurnacesReforged.MOD_ID, "jei/rock_generating");
		private static final int result = 2;
		private Component title;
		private final IDrawable background;
		protected IDrawableAnimated lava_anim;
		protected IDrawableAnimated water_anim;
		protected IDrawable lava_overlay;
		protected IDrawable water_overlay;
		private IIngredientType ingredientType;

		protected final IGuiHelper guiHelper;
		public static final ResourceLocation GUI = new ResourceLocation(BetterFurnacesReforged.MOD_ID , "textures/container/cobblestone_generator_gui.png");

		public CobblestoneGeneratorCategory(IGuiHelper guiHelper) {
			this.title = Registration.COBBLESTONE_GENERATOR.get().getName();
			this.background = guiHelper.createDrawable(GUI, 46, 21, 85, 52);
			this.guiHelper = guiHelper;
		}


		@Override
		public Class<? extends CobblestoneGeneratorRecipes> getRecipeClass() {
			return CobblestoneGeneratorRecipes.class;
		}

		@Override
		public void draw(CobblestoneGeneratorRecipes recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
			GuiUtil.renderScaled(stack,  (float) recipe.duration / 20 + "s", 62, 45, 0.75f, 0x7E7E7E, false);
			PoseStack newStack = stack;
			FluidRenderUtil.renderTiledFluid(stack,null, 12, 23, 17,12,new FluidStack(Fluids.LAVA, 1000), false);
			FluidRenderUtil.renderTiledFluid(stack,null, 55, 23, 17,12,new FluidStack(Fluids.WATER, 1000), true);
			this.lava_anim.draw(newStack, 12,23);
			this.lava_overlay.draw(newStack, 12,23);
			this.water_anim.draw(newStack, 55,23);
			this.water_overlay.draw(newStack, 55,23);
		}

		@Override
		public ResourceLocation getUid() {
			return Uid;
		}

		@Override
		public RecipeType<CobblestoneGeneratorRecipes> getRecipeType() {
			return Registration.ROCK_GENERATING_JEI;
		}

		@Override
		public Component getTitle() {
			return title;
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, CobblestoneGeneratorRecipes recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.OUTPUT,34, 24).addItemStack(recipe.getResultItem());
			builder.addSlot(RecipeIngredientRole.INPUT, 7, 6).addItemStack(new ItemStack(Items.LAVA_BUCKET));
			builder.addSlot(RecipeIngredientRole.INPUT,62, 6).addItemStack(new ItemStack(Items.WATER_BUCKET));
			// ...
			// ...
			this.lava_anim = guiHelper.drawableBuilder(GUI, 176, 24, 17, 12).buildAnimated(recipe.getDuration(), IDrawableAnimated.StartDirection.LEFT, false);
			this.lava_overlay = guiHelper.createDrawable(GUI, 176, 0, 17, 12);
			this.water_anim = guiHelper.drawableBuilder(GUI, 176, 36, 17, 12).buildAnimated(recipe.getDuration(), IDrawableAnimated.StartDirection.RIGHT, false);
			this.water_overlay = guiHelper.createDrawable(GUI, 176, 12, 17, 12);
		}
	}
}




