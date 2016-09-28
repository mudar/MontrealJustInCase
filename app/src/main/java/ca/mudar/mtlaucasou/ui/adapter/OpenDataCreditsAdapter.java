/*
    Montréal Just in Case
    Copyright (C) 2011  Mudar Noufal <mn@mudar.ca>

    Geographic locations of public safety services. A Montréal Open Data
    project.

    This file is part of Montréal Just in Case.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.mudar.mtlaucasou.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ca.mudar.mtlaucasou.R;
import ca.mudar.mtlaucasou.model.OpenDataLink;
import ca.mudar.mtlaucasou.util.IntentUtils;

import static ca.mudar.mtlaucasou.util.LogUtils.makeLogTag;

public class OpenDataCreditsAdapter extends ArrayAdapter<OpenDataLink> {

    private static final String TAG = makeLogTag("OpenDataCreditsAdapter");

    private final static int LINKS_COUNT = 5;

    private final LayoutInflater mInflater;
    @LayoutRes
    private final int mLayout;
    private final CreditsListCallback mCallback;

    public OpenDataCreditsAdapter(Context context, int resource, CreditsListCallback callback) {
        super(context, resource);

        mInflater = LayoutInflater.from(context);
        mLayout = resource;
        mCallback = callback;
    }

    /**
     * Should be equal to the number of sources +1 for the license.
     *
     * @return total number of items
     */
    @Override
    public int getCount() {
        return LINKS_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final OpenDataLink link = new OpenDataLink.Builder()
                .fromPosition(position)
                .build();

        final LinkViewHolder holder;
        if (convertView == null) {
            // Inflate a new view
            convertView = mInflater.inflate(mLayout, parent, false);
            holder = new LinkViewHolder(convertView, mCallback);

            // Cache the viewHolder object inside the new view
            convertView.setTag(holder);
        } else {
            // Recycle the view
            holder = (LinkViewHolder) convertView.getTag();
        }
        // Bind the data to the view
        holder.bind(link);

        return convertView;
    }

    private static class LinkViewHolder {
        private View itemView;
        private ImageView vIcon;
        private TextView vLink;
        private CreditsListCallback callback;

        public LinkViewHolder(View view, CreditsListCallback callback) {
            this.itemView = view;
            this.vIcon = (ImageView) view.findViewById(R.id.icon);
            this.vLink = (TextView) view.findViewById(R.id.title);
            this.callback = callback;
        }

        public void bind(final OpenDataLink link) {
            vLink.setText(link.getTitle());
            vIcon.setImageResource(link.getIcon());

            // Set the click listener to show remote website
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IntentUtils.showWebsite(view.getContext(), link.getUrl());
                    callback.onItemSelected();
                }
            });
        }
    }

    /**
     * Interface that allows closing the parent dialogFragment once a link is tapped
     */
    public interface CreditsListCallback {
        void onItemSelected();
    }
}
