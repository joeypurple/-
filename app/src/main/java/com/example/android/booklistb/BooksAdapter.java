package com.example.android.booklistb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by joe on 17/08/2016.
 */
public class BooksAdapter extends ArrayAdapter<Books> {

    public BooksAdapter(Context context, ArrayList<Books> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if the convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Find the book at the given position in the list of books
        Books currentBook = getItem(position);

        //Find the TextView with the view ID title
        TextView titleView = (TextView) listItemView.findViewById(R.id.title_result);
        titleView.setText(currentBook.title);

        //Find the TextView with the view ID author
        TextView authorView = (TextView) listItemView.findViewById(R.id.author_result);
        authorView.setText(currentBook.author);

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }


}
