/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import java.io.Serializable;

/**
 * <p>
 * {
 * "typ":"gads",
 * "link_id":"xxxxx",
 * "click_id":"xxxx",
 * "tm_click":"",
 * "scheme":"xxxxxx",
 * "v1":{"custom_params":{}},
 * "v2":{}
 * }
 *
 * @author cpacm 2022/8/4
 */
class AdvertData implements Serializable {

    private static final long serialVersionUID = -1L;

    public String linkID;
    public String clickID;
    public String clickTM;
    public String customParams;
    public long tm = 0L;

    public void copy(AdvertData another){
        this.linkID = another.linkID;
        this.clickID = another.clickID;
        this.clickTM = another.clickTM;
        this.customParams = another.customParams;
        this.tm = another.tm;
    }
}
