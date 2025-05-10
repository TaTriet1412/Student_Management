package com.example.quanlyhocvien;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlyhocvien.object.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LogViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Log>> logsLiveData = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<ArrayList<Log>> getLogsLiveData() {
        return logsLiveData;
    }

    public void setLogsLiveData(ArrayList<Log> logs) {
        logsLiveData.setValue(logs);
    }

    public void addLog(Log log) {
        ArrayList<Log> logs = logsLiveData.getValue();
        assert logs != null;
        logs.add(log);
        logsLiveData.setValue(logs);
    }

    public void removeLog(Log log) {
        ArrayList<Log> logs = logsLiveData.getValue();
        assert logs != null;
        logs.remove(log);
        logsLiveData.setValue(logs);
    }

    public void updateLog(Log log) {
        ArrayList<Log> logs = logsLiveData.getValue();
        assert logs != null;
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).getTime().equals(log.getTime())) {
                logs.set(i, log);
                break;
            }
        }
        logsLiveData.setValue(logs);
    }
}
