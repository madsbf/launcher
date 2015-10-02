package me.madsbf.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.shape.library.collections.adapters.RecyclerAdapter;
import me.madsbf.launcher.viewmodel.AppViewModel;

public class SortedAppRecyclerAdapter extends RecyclerAdapter<AppViewModel> {

    List<String> titles = new ArrayList<>();

    public int addSorted(AppViewModel item, int layoutResourceId) {
        titles.add(item.title.get());
        Collections.sort(titles);
        int index = titles.indexOf(item.title.get());
        super.add(index, item, layoutResourceId);
        return index;
    }
}
