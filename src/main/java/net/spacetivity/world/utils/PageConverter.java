package net.spacetivity.world.utils;

import com.google.common.collect.Lists;
import net.spacetivity.world.SpaceWorldManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class PageConverter {

    /**
     * With this method a user can access to a sublist in the page storage.
     *
     * @param player          is the requester of the sorted data
     * @param data            is the original unsorted data in one list which is then separated into paged by the splitTextToPages() method
     * @param title           page title
     * @param elementsPerPage identifier to manage the amount of items on each page
     * @param pageNumber      to identify which page the user will get
     * @param response        returns the {@link List<String>} with content of the page, which the user has requested in a {@link Consumer}
     */
    public void showPage(Player player, List<String> data, String title, int elementsPerPage, int pageNumber, Consumer<List<String>> response) {
        List<List<String>> pages = splitTextToPages(data, elementsPerPage);

        // if the user in game inserts one as the page number the system will
        // convert it to zero to show the first page in the list

        int securePageNumber = (pageNumber == 0) ? 0 : pageNumber - 1;

        if (securePageNumber > pages.size()) {
            player.sendMessage(SpaceWorldManager.PREFIX + "This page doesn't exist.");
            return;
        }

        List<String> contentOfPage = pages.get(securePageNumber);
        player.sendMessage(SpaceWorldManager.PREFIX + title + "Page §f" + (pageNumber == 0 ? 1 : pageNumber) + "§7/§f" + pages.size());
        response.accept(contentOfPage);
    }

    /**
     * This method converts a {@link List<String>} into sub-lists for different pages
     * Each sublist has the amount of items like the elementsPerPage identifier declares
     *
     * @param data            is the original List with the original unsorted data
     * @param elementsPerPage is the identifier to know how many items should be stored in a single sub-list.
     * @return the sorted {@link List} with the data stored in sub-lists
     */
    public List<List<String>> splitTextToPages(List<String> data, int elementsPerPage) {
        return Lists.partition(data, elementsPerPage);
    }
}
