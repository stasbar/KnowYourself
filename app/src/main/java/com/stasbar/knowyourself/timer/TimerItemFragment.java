/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stasbar.knowyourself.timer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stasbar.knowyourself.R;
import com.stasbar.knowyourself.data.DataModel;
import com.stasbar.knowyourself.data.Timer;


public class TimerItemFragment extends Fragment {

    private static final String KEY_TIMER_ID = "KEY_TIMER_ID";
    private int mTimerId;

    /** The public no-arg constructor required by all fragments. */
    public TimerItemFragment() {}

    public static TimerItemFragment newInstance(Timer timer) {
        final TimerItemFragment fragment = new TimerItemFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_TIMER_ID, timer.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimerId = getArguments().getInt(KEY_TIMER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final Timer timer = getTimer();
        if (timer == null) {
            return null;
        }

        final TimerItem view = (TimerItem) inflater.inflate(R.layout.timer_item, container, false);
        view.findViewById(R.id.reset_add).setOnClickListener(new ResetAddListener());
        view.update(timer);

        return view;
    }

    /**
     * @return {@code true} iff the timer is in a state that requires continuous updates
     */
    boolean updateTime() {
        final TimerItem view = (TimerItem) getView();
        if (view != null) {
            final Timer timer = getTimer();
            view.update(timer);
            return !timer.isReset();
        }

        return false;
    }

    int getTimerId() {
        return mTimerId;
    }

    Timer getTimer() {
        return DataModel.getDataModel().getTimer(getTimerId());
    }

    private class ResetAddListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final Timer timer = getTimer();
            if (timer.isPaused()) {
                DataModel.getDataModel().resetOrDeleteTimer(timer, R.string.label_deskclock);
            } else if (timer.isRunning() || timer.isExpired() || timer.isMissed()) {
                DataModel.getDataModel().addTimerMinute(timer);
            }
        }
    }


}