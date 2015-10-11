package me.madsbf.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.shape.library.collections.adapters.RecyclerAdapter;
import me.madsbf.launcher.viewmodel.AppViewModel;

public class SortedAppRecyclerAdapter extends RecyclerAdapter<AppViewModel> {

    List<String> titles = new ArrayList<>();

    public int addSorted(AppViewModel item, int layoutResourceId) {
        titles.add(item.title.get().toLowerCase());
        Collections.sort(titles);
        int index = titles.indexOf(item.title.get().toLowerCase());
        super.add(index, item, layoutResourceId);
        return index;
    }

    @Override
    public void remove(AppViewModel item) {
        super.remove(item);
        titles.remove(indexOf(item));
    }

    @Override
    public void remove(int position) {
        super.remove(position);
        titles.remove(position);
    }
}
