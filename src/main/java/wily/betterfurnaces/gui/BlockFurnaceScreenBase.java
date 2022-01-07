package wily.betterfurnaces.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;
import wily.betterfurnaces.BetterFurnacesReforged;
import wily.betterfurnaces.container.BlockForgeContainerBase;
import wily.betterfurnaces.container.BlockFurnaceContainerBase;
import wily.betterfurnaces.init.Registration;
import wily.betterfurnaces.network.Messages;
import wily.betterfurnaces.network.PacketSettingsButton;
import wily.betterfurnaces.network.PacketShowSettingsButton;
import wily.betterfurnaces.util.FluidRenderUtil;
import wily.betterfurnaces.util.StringHelper;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class BlockFurnaceScreenBase<T extends BlockFurnaceContainerBase> extends ContainerScreen<T> {

    public ResourceLocation GUI = new ResourceLocation(BetterFurnacesReforged.MOD_ID + ":" + "textures/container/furnace_gui.png");
    public static final ResourceLocation WIDGETS = new ResourceLocation(BetterFurnacesReforged.MOD_ID + ":" + "textures/container/widgets.png");
    PlayerInventory playerInv;
    ITextComponent name;


    public boolean add_button;
    public boolean sub_button;

    public BlockFurnaceScreenBase(T t, PlayerInventory inv, ITextComponent name) {
        super(t, inv, name);
        playerInv = inv;
        this.name = name;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrix, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
    }


    @Override
    protected void renderLabels(MatrixStack matrix, int mouseX, int mouseY) {
        int actualMouseX = mouseX - ((this.width - this.getXSize()) / 2);
        int actualMouseY = mouseY - ((this.height - this.getYSize()) / 2);
        this.minecraft.font.draw(matrix, this.playerInv.getDisplayName(), 7, this.getYSize() - 93, 4210752);
        this.minecraft.font.draw(matrix, name, 7 + this.getXSize() / 2 - this.minecraft.font.width(name.getString()) / 2, 6, 4210752);
        if (getMenu().te.hasUpgrade(Registration.LIQUID.get()) &&
                (mouseX > getGuiLeft() + 73 && mouseX < getGuiLeft() + 92 && mouseY > getGuiTop() + 49 && mouseY < getGuiTop() + 70))
            this.renderTooltip(matrix, new StringTextComponent((this.getMenu()).getFluidStackStored(false).getDisplayName().getString() +": " + ((BlockFurnaceContainerBase) this.getMenu()).getFluidStackStored(false).getAmount() + " mB"), actualMouseX, actualMouseY);
        if (getMenu().te.hasUpgrade(Registration.ENERGY.get()) &&
                (mouseX > getGuiLeft() + 31 && mouseX < getGuiLeft() + 47 && mouseY > getGuiTop() + 17 && mouseY < getGuiTop() + 51))
            this.renderTooltip(matrix, new StringTextComponent((this.getMenu()).getEnergyStored()/1000 + " kFE/" + ( this.getMenu()).getEnergyMaxStored()/1000 + "kFE"), actualMouseX, actualMouseY);
        if (getMenu().te.hasUpgrade(Registration.FACTORY.get())) {
            if (this.getMenu().showInventoryButtons() && this.getMenu().getRedstoneMode() == 4) {
                int comSub = this.getMenu().getComSub();
                int i = comSub > 9 ? 28 : 31;
                this.minecraft.font.draw(matrix, new StringTextComponent("" + comSub), i - 42, 138, 4210752);
            }
        }
        if (getMenu().te.hasUpgrade(Registration.XP.get()) &&
                (mouseX > getGuiLeft() + 116 && mouseX < getGuiLeft() + 132 && mouseY > getGuiTop() + 57 && mouseY < getGuiTop() + 73))
            this.renderTooltip(matrix, new StringTextComponent(this.getMenu().getFluidStackStored(true).getDisplayName().getString() +": " + (this.getMenu()).getFluidStackStored(true).getAmount() + " mB"), actualMouseX, actualMouseY);

        this.addTooltips(matrix, actualMouseX, actualMouseY);


    }

    private void addTooltips(MatrixStack matrix, int mouseX, int mouseY) {
        if (!getMenu().showInventoryButtons()) {
            if (mouseX >= -20 && mouseX <= 0 && mouseY >= 52 && mouseY <= 74) {
                this.renderTooltip(matrix, new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_open"), mouseX, mouseY);
            }
        } else {
            if (mouseX >= -13 && mouseX <= 0 && mouseY >= 52 && mouseY <= 74) {
                this.renderComponentTooltip(matrix, StringHelper.getShiftInfoGui(), mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 60 && mouseY <= 73) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_auto_input"));
                list.add(new StringTextComponent("" + this.getMenu().getAutoInput()));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -29 && mouseX <= -16 && mouseY >= 60 && mouseY <= 73) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_auto_output"));
                list.add(new StringTextComponent("" + this.getMenu().getAutoOutput()));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 79 && mouseY <= 88) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_top"));
                list.add(this.getMenu().getTooltip(1));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 103 && mouseY <= 112) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_bottom"));
                list.add(this.getMenu().getTooltip(0));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 91 && mouseY <= 100) {
                List<ITextComponent> list = Lists.newArrayList();
                if (isShiftKeyDown()) {
                    list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_reset"));
                } else {
                    list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_front"));
                    list.add(this.getMenu().getTooltip(this.getMenu().getIndexFront()));
                }
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -44 && mouseX <= -35 && mouseY >= 91 && mouseY <= 100) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_left"));
                list.add(this.getMenu().getTooltip(this.getMenu().getIndexLeft()));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 91 && mouseY <= 100) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_right"));
                list.add(this.getMenu().getTooltip(this.getMenu().getIndexRight()));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 103 && mouseY <= 112) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_back"));
                list.add(this.getMenu().getTooltip(this.getMenu().getIndexBack()));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 118 && mouseY <= 131) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_redstone_ignored"));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -31 && mouseX <= -18 && mouseY >= 118 && mouseY <= 131) {
                List<ITextComponent> list = Lists.newArrayList();
                if (isShiftKeyDown()) {
                    list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_redstone_low"));
                } else {
                    list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_redstone_high"));
                }
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -15 && mouseX <= -2 && mouseY >= 118 && mouseY <= 131) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_redstone_comparator"));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 134 && mouseY <= 147) {
                List<ITextComponent> list = Lists.newArrayList();
                list.add(new TranslationTextComponent("tooltip." + BetterFurnacesReforged.MOD_ID + ".gui_redstone_comparator_sub"));
                this.renderComponentTooltip(matrix, list, mouseX, mouseY);
            }

        }
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(GUI);
        int relX = (this.width - this.getXSize()) / 2;
        int relY = (this.height - this.getYSize()) / 2;
        this.blit(matrix, relX, relY, 0, 0, this.getXSize(), this.getYSize());
        int i;
        if (((BlockFurnaceContainerBase) this.getMenu()).BurnTimeGet() > 0) {
            i = ((BlockFurnaceContainerBase) this.getMenu()).getBurnLeftScaled(13);
            this.blit(matrix, getGuiLeft() + 55, getGuiTop() + 37 + 12 - i, 176, 12 - i, 14, i + 1);
        }

        i = ((BlockFurnaceContainerBase) this.getMenu()).getCookScaled(24);
        this.blit(matrix, getGuiLeft() + 79, getGuiTop() + 34, 176, 14, i + 1, 16);
        if (getMenu().te.hasUpgrade(Registration.ENERGY.get())){
            Minecraft.getInstance().getTextureManager().bind(WIDGETS);
            i = ( this.getMenu()).getEnergyStoredScaled(34);
            this.blit(matrix, getGuiLeft() + 31, getGuiTop() + 17, 240, 0, 16, 34);
            this.blit(matrix, getGuiLeft() + 31, getGuiTop() + 17, 240, 34, 16, 34-i);
        }
        if (getMenu().te.hasUpgrade(Registration.LIQUID.get())) {
            Minecraft.getInstance().getTextureManager().bind(WIDGETS);
            this.blit(matrix, getGuiLeft() + 73, getGuiTop() + 49, 192, 38, 20, 22);
            FluidStack fluid =  ((BlockFurnaceContainerBase) this.getMenu()).getFluidStackStored(false);
            i = ((BlockFurnaceContainerBase) this.getMenu()).getFluidStoredScaled(21,false);
            if (i > 0) {
                FluidRenderUtil.renderTiledFluid(matrix, this, 73, 49, 20, 22, fluid, false);
                Minecraft.getInstance().getTextureManager().bind(WIDGETS);
                this.blit(matrix, getGuiLeft() + 73, getGuiTop() + 49, 192, 38, 20, 22-i);

            }
            Minecraft.getInstance().getTextureManager().bind(WIDGETS);
            this.blit(matrix, getGuiLeft() + 73, getGuiTop() + 49, 192, 16, 20, 22);
        }

        if (this.getMenu().te.hasUpgrade(Registration.XP.get())) {
            Minecraft.getInstance().getTextureManager().bind(WIDGETS);
            this.blit(matrix, getGuiLeft() + 116, getGuiTop() + 57, 208, 0, 16, 16);
            FluidStack fluid =  (this.getMenu()).getFluidStackStored(true);
            i = (this.getMenu()).getFluidStoredScaled(16,true);
            if (i > 0) {
                FluidRenderUtil.renderTiledFluid(matrix, this, 116, 57, 16, 16, fluid, false);
                Minecraft.getInstance().getTextureManager().bind(WIDGETS);
                this.blit(matrix, getGuiLeft() + 116, getGuiTop() + 57, 208, 0, 16, 16-i);

            }
            Minecraft.getInstance().getTextureManager().bind(WIDGETS);
            this.blit(matrix, getGuiLeft() + 116, getGuiTop() + 57, 192, 0, 16, 16);
        }
        if (((BlockFurnaceContainerBase) this.getMenu()).slots.get(5).getItem().getItem() == new ItemStack(Registration.FACTORY.get()).getItem()) {
            this.minecraft.getTextureManager().bind(WIDGETS);
            int actualMouseX = mouseX - ((this.width - this.getXSize()) / 2);
            int actualMouseY = mouseY - ((this.height - this.getYSize()) / 2);

            this.addInventoryButtons(matrix, actualMouseX, actualMouseY);
            this.addRedstoneButtons(matrix, actualMouseX, actualMouseY);
        }
    }


    private void addRedstoneButtons(MatrixStack matrix, int mouseX, int mouseY) {
        if (this.getMenu().showInventoryButtons()) {
            this.blitRedstone(matrix);
            if (this.getMenu().getRedstoneMode() == 4) {
                int comSub = getMenu().getComSub();
                boolean flag = isShiftKeyDown();
                if (flag) {
                    if (comSub > 0) {
                        this.sub_button = true;
                        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 86 && mouseY <= 147) {
                            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 14, 0, 14, 14);
                        } else {
                            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 0, 0, 14, 14);
                        }
                    } else {
                        this.sub_button = false;
                        this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 28, 0, 14, 14);
                    }

                } else {
                    if (comSub < 15) {
                        this.add_button = true;
                        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 134 && mouseY <= 147) {
                            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 14, 14, 14, 14);
                        } else {
                            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 0, 14, 14, 14);
                        }
                    } else {
                        this.add_button = false;
                        this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 134, 28, 14, 14, 14);

                    }
                }
            }
        }
    }

    private void addInventoryButtons(MatrixStack matrix, int mouseX, int mouseY) {
        if (!getMenu().showInventoryButtons()) {
            this.blit(matrix, getGuiLeft() - 13, getGuiTop() + 52, 0, 28, 16, 19);
        } else if (getMenu().showInventoryButtons()) {
            this.blit(matrix, getGuiLeft() - 56, getGuiTop() + 52, 0, 47, 59, 107);
            if (mouseX >= -47 && mouseX <= -34 && mouseY >= 60 && mouseY <= 73 || this.getMenu().getAutoInput()) {
                this.blit(matrix, getGuiLeft() - 47, getGuiTop() + 60, 0, 189, 14, 14);
            }
            if (mouseX >= -29 && mouseX <= -16 && mouseY >= 60 && mouseY <= 73 || this.getMenu().getAutoOutput()) {
                this.blit(matrix, getGuiLeft() - 29, getGuiTop() + 60, 14, 189, 14, 14);
            }
            this.blitIO(matrix);
        }


    }

    private void blitRedstone(MatrixStack matrix) {
        boolean flag = isShiftKeyDown();
        if (flag) {
            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 118, 84, 189, 14, 14);
        }
        int setting = this.getMenu().getRedstoneMode();
        if (setting == 0) {
            this.blit(matrix, getGuiLeft() - 47, getGuiTop() + 118, 28, 189, 14, 14);
        } else if (setting == 1 && !flag) {
            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 118, 42, 189, 14, 14);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 31, getGuiTop() + 118, 98, 189, 14, 14);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 15, getGuiTop() + 118, 56, 189, 14, 14);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 47, getGuiTop() + 134, 70, 189, 14, 14);
        }

    }

    private void blitIO(MatrixStack matrix) {
        int[] settings = new int[]{0, 0, 0, 0, 0, 0};
        int setting = this.getMenu().getSettingTop();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 79, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 79, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 79, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 79, 30, 161, 10, 10);
        }
        settings[1] = setting;

        setting = this.getMenu().getSettingBottom();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 103, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 103, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 103, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 103, 30, 161, 10, 10);
        }
        settings[0] = setting;
        setting = this.getMenu().getSettingFront();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 91, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 91, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 91, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 32, getGuiTop() + 91, 30, 161, 10, 10);
        }
        settings[this.getMenu().getIndexFront()] = setting;
        setting = this.getMenu().getSettingBack();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 103, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 103, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 103, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 103, 30, 161, 10, 10);
        }
        settings[this.getMenu().getIndexBack()] = setting;
        setting = this.getMenu().getSettingLeft();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 44, getGuiTop() + 91, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 44, getGuiTop() + 91, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 44, getGuiTop() + 91, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 44, getGuiTop() + 91, 30, 161, 10, 10);
        }
        settings[this.getMenu().getIndexLeft()] = setting;
        setting = this.getMenu().getSettingRight();
        if (setting == 1) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 91, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 91, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 91, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.blit(matrix, getGuiLeft() - 20, getGuiTop() + 91, 30, 161, 10, 10);
        }
        settings[this.getMenu().getIndexRight()] = setting;
        boolean input = false;
        boolean output = false;
        boolean both = false;
        boolean fuel = false;
        for (int set : settings) {
            if (set == 1) {
                input = true;
            } else if (set == 2) {
                output = true;
            } else if (set == 3) {
                both = true;
            } else if (set == 4) {
                fuel = true;
            }
        }
        if (input || both) {
            this.blit(matrix, getGuiLeft() + 53, getGuiTop() + 17, 0, 171, 18, 18);
        }
        if (output || both) {
            this.blit(matrix, getGuiLeft() + 111, getGuiTop() + 30, 0, 203, 26, 26);
        }
        if (fuel) {
            this.blit(matrix, getGuiLeft() + 53, getGuiTop() + 53, 18, 171, 18, 18);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double actualMouseX = mouseX - ((this.width - this.getXSize()) / 2);
        double actualMouseY = mouseY - ((this.height - this.getYSize()) / 2);
        this.mouseClickedRedstoneButtons(actualMouseX, actualMouseY);
        this.mouseClickedInventoryButtons(button, actualMouseX, actualMouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseClickedInventoryButtons(int button, double mouseX, double mouseY) {
        boolean flag = button == GLFW.GLFW_MOUSE_BUTTON_2;
        if (!getMenu().showInventoryButtons()) {
            if (mouseX >= -10 && mouseX <= 0 && mouseY >= 52 && mouseY <= 74) {
                Messages.INSTANCE.sendToServer(new PacketShowSettingsButton(this.getMenu().getPos(), 1));
                Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            }
        } else {
            if (mouseX >= -10 && mouseX <= 0 && mouseY >= 52 && mouseY <= 74) {
                Messages.INSTANCE.sendToServer(new PacketShowSettingsButton(this.getMenu().getPos(), 0));
                Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 60 && mouseY <= 73) {
                if (!this.getMenu().getAutoInput()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 6, 1));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                } else {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 6, 0));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }

            } else if (mouseX >= -29 && mouseX <= -16 && mouseY >= 60 && mouseY <= 73) {
                if (!this.getMenu().getAutoOutput()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 7, 1));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                } else {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 7, 0));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 79 && mouseY <= 88) {
                if (flag) {
                    sendToServerInverted(this.getMenu().getSettingTop(), 1);
                } else {
                    sendToServer(this.getMenu().getSettingTop(), 1);
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 103 && mouseY <= 112) {
                if (flag) {
                    sendToServerInverted(this.getMenu().getSettingBottom(), 0);
                } else {
                    sendToServer(this.getMenu().getSettingBottom(), 0);
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 91 && mouseY <= 100) {
                if (isShiftKeyDown()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 0, 0));
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 1, 0));
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 2, 0));
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 3, 0));
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 4, 0));
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 5, 0));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.8F, 0.3F));
                } else {
                    if (flag) {
                        sendToServerInverted(this.getMenu().getSettingFront(), this.getMenu().getIndexFront());
                    } else {
                        sendToServer(this.getMenu().getSettingFront(), this.getMenu().getIndexFront());
                    }
                }
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 103 && mouseY <= 112) {
                if (flag) {
                    sendToServerInverted(this.getMenu().getSettingBack(), this.getMenu().getIndexBack());
                } else {
                    sendToServer(this.getMenu().getSettingBack(), this.getMenu().getIndexBack());
                }
            } else if (mouseX >= -44 && mouseX <= -35 && mouseY >= 91 && mouseY <= 100) {
                if (flag) {
                    sendToServerInverted(this.getMenu().getSettingLeft(), this.getMenu().getIndexLeft());
                } else {
                    sendToServer(this.getMenu().getSettingLeft(), this.getMenu().getIndexLeft());
                }
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 91 && mouseY <= 100) {
                if (flag) {
                    sendToServerInverted(this.getMenu().getSettingRight(), this.getMenu().getIndexRight());
                } else {
                    sendToServer(this.getMenu().getSettingRight(), this.getMenu().getIndexRight());
                }
            }
        }
    }

    private void sendToServer(int setting, int index) {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
        if (setting <= 0) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 1));
        } else if (setting == 1) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 2));
        } else if (setting == 2) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 3));
        } else if (setting == 3) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 4));
        } else if (setting >= 4) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 0));
        }
    }

    private void sendToServerInverted(int setting, int index) {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.3F, 0.3F));
        if (setting <= 0) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 4));
        } else if (setting == 1) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 0));
        } else if (setting == 2) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 1));
        } else if (setting == 3) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 2));
        } else if (setting >= 4) {
            Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), index, 3));
        }
    }

    public void mouseClickedRedstoneButtons(double mouseX, double mouseY) {
        if (getMenu().showInventoryButtons()) {
            if (mouseX >= -31 && mouseX <= -18 && mouseY >= 132 && mouseY <= 147) {
                if (this.sub_button && isShiftKeyDown()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 9, this.getMenu().getComSub() - 1));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.3F, 0.3F));
                }
            }
            if (mouseX >= -31 && mouseX <= -18 && mouseY >= 132 && mouseY <= 147) {
                if (this.add_button && !isShiftKeyDown()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 9, this.getMenu().getComSub() + 1));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            }
            if (mouseX >= -47 && mouseX <= -34 && mouseY >= 118 && mouseY <= 131) {
                if (this.getMenu().getRedstoneMode() != 0) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 8, 0));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            }

            if (mouseX >= -31 && mouseX <= -18 && mouseY >= 118 && mouseY <= 131) {
                if (this.getMenu().getRedstoneMode() != 1 && !isShiftKeyDown()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 8, 1));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
                if (this.getMenu().getRedstoneMode() != 2 && isShiftKeyDown()) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 8, 2));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            }

            if (mouseX >= -15 && mouseX <= -2 && mouseY >= 118 && mouseY <= 131) {
                if (this.getMenu().getRedstoneMode() != 3) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 8, 3));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            }

            if (mouseX >= -47 && mouseX <= -34 && mouseY >= 134 && mouseY <= 147) {
                if (this.getMenu().getRedstoneMode() != 4) {
                    Messages.INSTANCE.sendToServer(new PacketSettingsButton(this.getMenu().getPos(), 8, 4));
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }

            }
        }
    }

    public static boolean isShiftKeyDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean isKeyDown(int glfw) {
        InputMappings.Input key = InputMappings.Type.KEYSYM.getOrCreate(glfw);
        int keyCode = key.getValue();
        if (keyCode != InputMappings.UNKNOWN.getValue()) {
            long windowHandle = Minecraft.getInstance().getWindow().getWindow();
            try {
                if (key.getType() == InputMappings.Type.KEYSYM) {
                    return InputMappings.isKeyDown(windowHandle, keyCode);
                } /**else if (key.getType() == InputMappings.Type.MOUSE) {
                 return GLFW.glfwGetMouseButton(windowHandle, keyCode) == GLFW.GLFW_PRESS;
                 }**/
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
