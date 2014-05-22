/*
 * Copyright 2012-2013, Arno Puder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.puder.trs80;

import android.content.Context;
import android.content.SharedPreferences;

public class ROMs {

    static private SharedPreferences sharedPrefs;

    static {
        sharedPrefs = TRS80Application.getAppContext().getSharedPreferences(
                SettingsActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    static public boolean hasROMs() {
        return hasModel1ROM() && hasModel3ROM();
    }

    static public boolean hasModel1ROM() {
        return sharedPrefs.getString(SettingsActivity.CONF_ROM_MODEL1, null) != null;
    }

    static public boolean hasModel3ROM() {
        return sharedPrefs.getString(SettingsActivity.CONF_ROM_MODEL3, null) != null;
    }

}
