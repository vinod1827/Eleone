package com.morgat.eleone.listeners;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/4/2019.
 */

public interface ApiCallbackListener {

    void onResponseArrayReceived(ArrayList arrayList);

    void onSuccess(String responce);

    void onFailure(String responce);


}
