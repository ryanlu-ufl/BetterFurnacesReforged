package wily.betterfurnaces.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import wily.betterfurnaces.blockentity.InventoryBlockEntity;


public abstract class AbstractInventoryMenu<T extends InventoryBlockEntity> extends AbstractContainerMenu {

    public T be;
    protected ContainerData fields;
    protected Player playerEntity;
    protected IItemHandler playerInventory;
    protected final Level world;
    protected int TOP_ROW = 84;



    public AbstractInventoryMenu(MenuType<?> containerType, int windowId, Level world, BlockPos pos, Inventory playerInventory, Player player, ContainerData fields) {
        super(containerType, windowId);
        this.be = (T) world.getBlockEntity(pos);

        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.world = playerInventory.player.level;
        this.fields = fields;
        this.addInventorySlots();
        layoutPlayerInventorySlots(8, TOP_ROW);
        this.addDataSlots(this.fields);
        checkContainerSize(this.be, be.getInvSize);
    }

    public void addInventorySlots(){

    }

    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int i, int j, boolean bl) {
        boolean bl2 = false;
        int k = i;
        if (bl) {
            k = j - 1;
        }

        Slot slot;
        ItemStack itemStack2;
        if (itemStack.isStackable()) {
            while(!itemStack.isEmpty()) {
                if (bl) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (!itemStack2.isEmpty() && ItemStack.isSameItemSameTags(itemStack, itemStack2) && slot.mayPlace(itemStack)) {
                    int l = itemStack2.getCount() + itemStack.getCount();
                    if (l <= itemStack.getMaxStackSize()) {
                        itemStack.setCount(0);
                        itemStack2.setCount(l);
                        slot.setChanged();
                        bl2 = true;
                    } else if (itemStack2.getCount() < itemStack.getMaxStackSize()) {
                        itemStack.shrink(itemStack.getMaxStackSize() - itemStack2.getCount());
                        itemStack2.setCount(itemStack.getMaxStackSize());
                        slot.setChanged();
                        bl2 = true;
                    }
                }

                if (bl) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (!itemStack.isEmpty()) {
            if (bl) {
                k = j - 1;
            } else {
                k = i;
            }

            while(true) {
                if (bl) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = this.slots.get(k);
                itemStack2 = slot.getItem();
                if (itemStack2.isEmpty() && slot.mayPlace(itemStack)) {
                    if (itemStack.getCount() > slot.getMaxStackSize()) {
                        slot.set(itemStack.split(slot.getMaxStackSize()));
                    } else {
                        slot.set(itemStack.split(itemStack.getCount()));
                    }

                    slot.setChanged();
                    bl2 = true;
                    break;
                }

                if (bl) {
                    --k;
                } else {
                    ++k;
                }
            }
        }
        return bl2;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < be.getInvSize) {
                if (!this.moveItemStackTo(stack, be.getInvSize, be.getInvSize + 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else if (!this.moveItemStackTo(stack, 0, be.getInvSize, true)){
                if (index < be.getInvSize + 27) {
                    if (!this.moveItemStackTo(stack, be.getInvSize + 27, be.getInvSize + 36, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < be.getInvSize + 36) {
                    if (!this.moveItemStackTo(stack, be.getInvSize, be.getInvSize + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }
        return itemstack;
    }



    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return be.stillValid(p_38874_);
    }
}
