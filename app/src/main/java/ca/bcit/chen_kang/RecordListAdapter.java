package ca.bcit.chen_kang;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.List;

public class RecordListAdapter extends ArrayAdapter<Record> {
    private Activity context;
    private List<Record> recordList;

    public RecordListAdapter(Activity context, List<Record> recordList) {
        super(context, R.layout.list_layout, recordList);
        this.context = context;
        this.recordList = recordList;
    }

    public RecordListAdapter(Context context, int resource, List<Record> objects, Activity context1, List<Record> recordList) {
        super(context, resource, objects);
        this.context = context1;
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView tvDate = listViewItem.findViewById(R.id.textViewDate);
        TextView tvUserId = listViewItem.findViewById(R.id.textViewUserId);
        TextView tvSr = listViewItem.findViewById(R.id.textViewSr);
        TextView tvDr = listViewItem.findViewById(R.id.textViewDr);
        TextView tvCondition = listViewItem.findViewById(R.id.textCondition);
        Record record = recordList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String curDate = dateFormat.format(record.getCurDate());
        tvUserId.setText(record.getUserId());
        tvDate.setText(curDate);
        tvSr.setText(record.getSrReading().toString());
        tvDr.setText(record.getDrReading().toString());
        tvCondition.setText(record.getCondition());
        return listViewItem;
    }

}


