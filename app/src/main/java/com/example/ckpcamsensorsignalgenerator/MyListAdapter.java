package com.example.ckpcamsensorsignalgenerator;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private ArrayList<String[]> fronts;
    private String[] temp;

    public class ViewHolder extends RecyclerView.ViewHolder {
        //first linear layout
        public TextView signalFrontNumberLabel;
        public EditText signalFrontNumberEditText;
        public CheckBox firstCrankRevCheckBox;
        public Button addButton;
        public Button removeButton;
        //second linear layout
        public EditText toothNumberEditText;
        public RadioGroup toothPlusRadioGroup;
        public RadioButton plus00radioButton;
        public RadioButton plus025radioButton;
        public RadioButton plus050radioButton;
        public RadioButton plus075radioButton;

        //ViewHolder Constructor
        public ViewHolder(View itemView) {
            super(itemView);
            //First line List Item GUI Elems
            signalFrontNumberLabel = (TextView)itemView.findViewById(R.id.signalFrontNumberLabel);
            signalFrontNumberEditText = (EditText) itemView.findViewById(R.id.signalFrontNumberEditText);
            firstCrankRevCheckBox = (CheckBox) itemView.findViewById(R.id.firstCrankRevCheckBox);
            addButton = (Button) itemView.findViewById(R.id.addButton);
            removeButton = (Button) itemView.findViewById(R.id.removeButton);
            //Second line List Item GUI Elems
            toothNumberEditText = (EditText) itemView.findViewById(R.id.toothNumberEditText);
            toothPlusRadioGroup = (RadioGroup) itemView.findViewById(R.id.toothPlusRadioGroup);
            plus00radioButton = (RadioButton) itemView.findViewById(R.id.plus00radioButton);
            plus025radioButton = (RadioButton) itemView.findViewById(R.id.plus025radioButton);
            plus050radioButton = (RadioButton) itemView.findViewById(R.id.plus050radioButton);
            plus075radioButton = (RadioButton) itemView.findViewById(R.id.plus075radioButton);
            //Add List Item
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    try {
                        temp = new String[]{"", "", "", ""};
                        fronts.add(position +1, temp);
                        notifyItemInserted(position +1);
                    } catch (ArrayIndexOutOfBoundsException e){e.printStackTrace();}
                }
            });
            //Remove List Item
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == 0) {
                        return;
                    }
                    try {
                        fronts.remove(position);
                        notifyItemRemoved(position);
                    }catch (ArrayIndexOutOfBoundsException e){e.printStackTrace();}
                }
            });
            //Get Values Of GUI Elems 
            signalFrontNumberEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = fronts.get(getAdapterPosition());
                    temp[0] = s.toString();
                    fronts.set(getAdapterPosition(),  temp);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            firstCrankRevCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    temp = fronts.get(getAdapterPosition());
                    fronts.set(getAdapterPosition(),  temp);
                    if (firstCrankRevCheckBox.isChecked()) {
                        temp[1] = "1";
                    } else {
                        temp[1] = "0";
                    }
                    fronts.set(getAdapterPosition(),  temp);
                }
            });

            toothNumberEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    temp = fronts.get(getAdapterPosition());
                    temp[2] = s.toString();
                    fronts.set(getAdapterPosition(), temp);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            toothPlusRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    temp = fronts.get(getAdapterPosition());
                    switch (checkedId) {
                        case R.id.plus00radioButton: temp[3] = "00";
                            break;
                        case R.id.plus025radioButton: temp[3] = "025";
                            break;
                        case R.id.plus050radioButton: temp[3] = "050";
                            break;
                        case R.id.plus075radioButton: temp[3] = "075";
                            break;

                        default:
                            break;
                    }

                    fronts.set(getAdapterPosition(), temp);
                }
            });
        }

    }
    //Adapter Constructor
    public MyListAdapter(ArrayList<String[]> fronts) {
        this.fronts = fronts;
    }

    @Override
    public int getItemCount() {
        return fronts.size();
    }
    //Inflate View From list_item.xml Layout
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(v);
    }
    //Set Values Of GUI Elems
    @Override
    public void onBindViewHolder(MyListAdapter.ViewHolder holder, int position) {
        temp = fronts.get(position);

        holder.signalFrontNumberEditText.setText(temp[0], TextView.BufferType.EDITABLE);

        if (temp[1].matches("1")) {
            holder.firstCrankRevCheckBox.setChecked(true);
        }

        holder.toothNumberEditText.setText(temp[2], TextView.BufferType.EDITABLE);

        switch (temp[3]) {
            case "00": {
                holder.toothPlusRadioGroup.clearCheck();
                holder.plus00radioButton.setChecked(true);
            }
                break;
            case "025": {
                holder.toothPlusRadioGroup.clearCheck();
                holder.plus025radioButton.setChecked(true);
            }
                break;
            case "050": {
                holder.toothPlusRadioGroup.clearCheck();
                holder.plus050radioButton.setChecked(true);
            }
                break;
            case "075": {
                holder.toothPlusRadioGroup.clearCheck();
                holder.plus075radioButton.setChecked(true);
            }
                break;

            default:
                break;
        }
    }
}
