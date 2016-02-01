package ru.jts_dev.gameserver.model;

import ru.jts_dev.common.id.IdPool;
import ru.jts_dev.gameserver.GameServerApplication;
import ru.jts_dev.gameserver.parser.data.item.ItemData;
import ru.jts_dev.gameserver.parser.data.item.ItemDatasHolder;

import javax.persistence.*;

/**
 * This class represent single character item. It has two main parts:
 * First is {@link ItemData} - immutable, static item info, loaded by
 * {@link ru.jts_dev.gameserver.parser.data.item.ItemDatasHolder} from itemdata.txt
 * Second - mutable fields, that represent current item status, count, enchant, inventory order, etc.
 * <p>
 * Creation of new item available through {@link #GameItem(int, ItemData)} constructor,
 * It is strongly recommended to create new item using {@link ru.jts_dev.gameserver.inventory.InventoryService}.
 *
 * @author Camelion
 * @since 17.01.16
 */
@Entity
public class GameItem {
    @Transient
    private int objectId;
    @Id
    @GeneratedValue
    private int id;
    @Column(name = "cnt")
    private long count;
    @Column
    private int enchant;
    @Column
    private boolean equipped;
    @Transient
    private ItemData itemData;

    /**
     * this constructor used only for JPA purposes, and can't be accessed externally
     */
    private GameItem() {
    }

    public GameItem(final int objectId, final ItemData itemData) {
        this.objectId = objectId;
        this.itemData = itemData;
    }

    public final int getObjectId() {
        return objectId;
    }

    public final ItemData getItemData() {
        return itemData;
    }

    public long getCount() {
        return count;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public int getEnchant() {
        return enchant;
    }

    /**
     * itemId property used only for JPA store/restore {@code itemData} template.
     *
     * @return - itemId of template.
     */
    @Access(AccessType.PROPERTY)
    @Column(name = "itemId")
    private int getItemId() {
        return itemData.getItemId();
    }

    /**
     * @param itemId - restored itemId
     * @see #getItemId()
     */
    private void setItemId(final int itemId) {
        final ItemDatasHolder itemDatasHolder = GameServerApplication.getBean(ItemDatasHolder.class);
        itemData = itemDatasHolder.getItemData().get(itemId);
    }

    @PostLoad
    public void postLoad() {
        final IdPool idPool = GameServerApplication.getBean("itemIdPool", IdPool.class);
        objectId = idPool.borrow();
    }

    /**
     * We should return {@code objectId} back to pool, because it not be needed anymore.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        final IdPool idPool = GameServerApplication.getBean("itemIdPool", IdPool.class);
        idPool.release(objectId);

        super.finalize();
    }
}
