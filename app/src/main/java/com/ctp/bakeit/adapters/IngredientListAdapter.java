package com.ctp.bakeit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.provider.BakeItContract;
import com.ctp.bakeit.utils.BakeItUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 3/10/18.
 */

public class IngredientListAdapter extends RecyclerView.Adapter<IngredientListAdapter.IngredientListViewHolder>{

    private Cursor mCursor;

    public IngredientListAdapter(Cursor mCursor) {
        this.mCursor = mCursor;
    }

    @Override
    public IngredientListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.ingredient_list_item,parent,false);
        return new IngredientListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(IngredientListViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        float quantity = mCursor.getFloat(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_QUANTITY));
        String quantityString = BakeItUtils.formatIngredientQuantity(Float.toString(quantity));
        String measure = mCursor.getString(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_MEASURE));
        measure = BakeItUtils.formatIngredientMeasure(measure);
        String ingredName = mCursor.getString(
                mCursor.getColumnIndex(BakeItContract.IngredientEntry.COLUMN_NAME));
        ingredName = BakeItUtils.getFormattedIngredientName(ingredName);

        String quantityView = quantityString + " " + measure;
        holder.ingredientQuantityView.setText(quantityView);
        holder.ingredientNameView.setText(ingredName);

    }

    @Override
    public int getItemCount() {
        if(mCursor==null) {
            return 0;
        }
        return mCursor.getCount();
    }


    public class IngredientListViewHolder extends RecyclerView.ViewHolder{


        @BindView(R.id.ingredient_list_item_quantity)
        TextView ingredientQuantityView;

        @BindView(R.id.ingredient_list_item_name)
        TextView ingredientNameView;

        public IngredientListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
