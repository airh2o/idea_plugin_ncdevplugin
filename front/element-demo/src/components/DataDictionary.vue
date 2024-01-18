<template>
  <div class="top">
    <el-container>
      <el-container>
        <el-aside class="left-div-aside" v-drag v-show="showLeftTree">
          <el-drawer
              title="易用性设置"
              :visible.sync="showSettingPanel"
              :direction="'ltr'"
              class="left-div-aside-setting"
          >
            <el-tag>树显示字段:</el-tag>
            <el-select
                class="left-treeShowNames"
                v-model="treeShowNames"
                clearable
                filterable
                multiple
                placeholder="树显示字段"
            >
              <el-option
                  v-for="item in treeShowNamesOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              >
              </el-option>
            </el-select>

            <el-tag>搜索哪些地方:</el-tag>
            <el-select
                class="left-searchPlaces"
                v-model="treeSearchPlaces"
                clearable
                filterable
                multiple
                placeholder="搜索哪些地方"
            >
              <el-option
                  v-for="item in treeSearchPlacesOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              >
              </el-option>
            </el-select>

            <el-tag>搜索大小写是否敏感:</el-tag>
            <el-switch
                class="left-searchIncloudProperts"
                v-model="searchAutoLower"
                active-text="不敏感"
                inactive-text="敏感"
            >
            </el-switch>

            <div>
              <el-tag>类名是否显示全名称:</el-tag>
              <el-switch
                  class="left-showSimpleClassName"
                  v-model="showSimpleClassName"
                  active-text="简单名称"
                  inactive-text="包+类名"
              >
              </el-switch>
            </div>

            <div>
              <el-tag>Tab标签只显示名称:</el-tag>
              <el-switch
                  class="left-showSimpleClassName"
                  v-model="tabTitleJustShowDisplayname"
                  active-text="名称"
                  inactive-text="名称+编码+表名称"
              >
              </el-switch>
            </div>

            <div>
              <el-checkbox v-model="notShowCalculation">不显示计算属性</el-checkbox>
            </div>
            <div>
              <el-checkbox v-model="notShowDynamic">不显示动态属性</el-checkbox>
            </div>

            <div>
              <el-tag>表格显示哪些列:</el-tag>
              <br>
              <el-checkbox v-model="tableShowColumns.name">属性编码</el-checkbox>
              <el-checkbox v-model="tableShowColumns.displayName">属性名称</el-checkbox>
              <el-checkbox v-model="tableShowColumns.fieldName">字段编码</el-checkbox>
              <el-checkbox v-model="tableShowColumns.fileTypeDesc">字段类型</el-checkbox>
              <el-checkbox v-model="tableShowColumns.nullable">可空</el-checkbox>
              <el-checkbox v-model="tableShowColumns.refModelDesc">引用模型</el-checkbox>
              <el-checkbox v-model="tableShowColumns.defaultValue">默认值</el-checkbox>
              <el-checkbox v-model="tableShowColumns.description">取值范围/枚举</el-checkbox>
            </div>
          </el-drawer>

          <el-input
              class="left-filterText"
              clearable
              resize="both"
              v-model="filterText"
              placeholder="搜索"
              size="small"
              :suffix-icon="Search"
          />
          <el-tree
              v-drag
              :default-expanded-keys="treeDefaultExpandedKeys"
              :default-checked-keys="treeDefaultExpandedKeys"
              class="filter-tree"
              node-key="id"
              :data="treeData"
              :props="defaultProps"
              :filter-node-method="filterNode"
              ref="tree"
              :highlight-current="true"
              @node-click="handleTreeNodeClick"
              empty-text="啥也没有找到啦"
          >
          </el-tree>
        </el-aside>

        <el-container class="main-div">
          <el-main class="main-button-backs-div">
            <div>
              <el-button
                  v-on:click="showSettingPanel = !showSettingPanel"
                  type="warning"
                  size="mini"
              >
                易用性设置
              </el-button>

              <el-button
                  link
                  size="mini"
                  type="primary"
                  v-on:click="showLeftTree = !showLeftTree"
              >隐藏/显示 左侧树
              </el-button>
              <el-button v-show="false" link size="mini" type="primary" v-on:click="switchExpandAll"
              >展开/收起 左侧树
              </el-button>
              <el-button link size="mini" type="primary" v-on:click="expandByOpenTabs"
              >{{ `左侧树展开到打开的页签` }}
              </el-button>

              <el-button link size="mini" type="danger" v-on:click="closeAllTab"
              >关闭所有打开的标签: {{ tabsList.length }}
              </el-button>

              <el-button link size="mini" type="info" v-on:click="gotoBack"
              >后退
              </el-button>
              <el-button link size="mini" type="info" v-on:click="gotoForward"
              >前进
              </el-button>
            </div>

            <el-tabs
                class="main-tabs"
                v-model="showTabsValue"
                type="card"
                closable
                @edit="handleTabsEdit"
            >
              <el-tab-pane
                  :key="item.name"
                  v-for="(item, index) in tabsList"
                  :label="item.title"
                  :name="item.name"
              >
                <div class="main-title-div">
                  <el-link
                      v-if="item.nowClass && item.nowClass.displayName"
                      link
                      type="primary"
                  >
                    {{ buildTableClassTypeName(item) }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.displayName"
                      type="success"
                  >{{ `| 表: ${item.nowClass.defaultTableName}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.displayName"
                      link
                      type="info"
                  >{{ `| VO类: ${item.nowClass.fullClassName}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.aggFullClassName"
                      link
                      type="info"
                  >{{ `| Agg类: ${item.nowClass.aggFullClassName}` }}
                  </el-link>

                  <el-link
                      v-if="item.nowClass && item.nowClass.pk_billtypecode"
                      link
                      type="danger"
                  >{{ `| 单据编码: ${item.nowClass.pk_billtypecode}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.billtypename"
                      link
                      type="danger"
                  >{{ `| 单据名称: ${item.nowClass.billtypename}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.nodecode"
                      link
                      type="info"
                  >{{ `| 节点编码: ${item.nowClass.nodecode}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.fun_name"
                      link
                      type="info"
                  >{{ `| 重量端节点名: ${item.nowClass.fun_name}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.paramvalue"
                      link
                      type="info"
                  >{{ `| 重量端XML配置: ${item.nowClass.paramvalue}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.pagecode"
                      link
                      type="info"
                  >{{ `| 轻量端页码编码: ${item.nowClass.pagecode}` }}
                  </el-link>
                  <el-link
                      v-if="item.nowClass && item.nowClass.pageurl"
                      link
                      type="info"
                  >{{ `| 轻量端页码地址: ${item.nowClass.pageurl}` }}
                  </el-link>
                  <br/>

                  <div
                      v-if="item.nowCompoment && item.nowCompoment.classDTOS"
                      class="goto-button-div"
                  >
                    <el-button
                        class="goto-button"
                        link
                        size="mini"
                        type="danger"
                        v-for="cc in item.nowCompoment.classDTOS"
                        :key="cc.id"
                        v-on:click="gotoClass(cc, 2)"
                    >
                      {{
                        cc.name +
                        " " +
                        cc.displayName +
                        " " +
                        (showSimpleClassName
                            ? cc.fullClassName.substr(cc.fullClassName.lastIndexOf(".") + 1)
                            : cc.fullClassName)
                      }}
                    </el-button>
                  </div>
                </div>

                <div class="props-table-div">
                  <el-table
                      class="props-table"
                      border
                      stripe
                      ref="filterTable"
                      :highlight-current-row="true"
                      :data="item.tableData"
                      :row-class-name="tableRowClassName"
                      style="width: 100%"
                  >
                    <el-table-column type="index"/>
                    <el-table-column
                        prop="name"
                        v-if="tableShowColumns.name"
                        label="属性编码"
                        sortable
                        width="180"
                        column-key="name"
                        :filters="nameFilters"
                        :filter-method="nameFilterMethod"
                        :show-overflow-tooltip="true"
                    />
                    <el-table-column
                        prop="displayName"
                        label="属性名称" v-if="tableShowColumns.displayName"
                        sortable
                        :filters="displayNameFilters"
                        :filter-method="displayNameFilterMethod"
                        :show-overflow-tooltip="true"
                        width="180"
                    />
                    <el-table-column
                        prop="fieldName"
                        label="字段编码" v-if="tableShowColumns.fieldName"
                        sortable
                        :filters="fieldNameFilters"
                        :filter-method="fieldNameFilterMethod"
                        :show-overflow-tooltip="true"
                        width="180"
                    />
                    <el-table-column
                        prop="fileTypeDesc"
                        label="字段类型" v-if="tableShowColumns.fileTypeDesc"
                        sortable
                        :filters="fileTypeDescFilters"
                        :filter-method="fileTypeDescFilterMethod"
                        :show-overflow-tooltip="true"
                        width="180"
                    />
                    <el-table-column
                        prop="nullable" v-if="tableShowColumns.nullable"
                        label="可空"
                        sortable
                        :filters="nullableFilters"
                        :filter-method="nullableFilterMethod"
                        :show-overflow-tooltip="true"
                        width="90"
                    />
                    <el-table-column
                        prop="refModelDesc" v-if="tableShowColumns.refModelDesc"
                        label="引用模型"
                        sortable
                        :filters="refModelDescFilters"
                        :filter-method="refModelDescFilterMethod"
                        :show-overflow-tooltip="true"
                        width="280"
                    >
                      <template slot-scope="scope">
                        <el-link
                            link
                            :type="scope.row.refModelName ? 'primary' : 'info'"
                            :disabled="!scope.row.refModelName"
                            v-on:click="gotoClass(scope.row, 1)"
                        >
                          {{
                            buildTableRefModelDescColumnValue(scope.row)
                          }}
                          <el-icon class="el-icon--right">
                            <icon-view/>
                          </el-icon>
                        </el-link>
                      </template>
                    </el-table-column>
                    <el-table-column
                        prop="defaultValue"
                        label="默认值" v-if="tableShowColumns.defaultValue"
                        sortable
                        :filters="defaultValueFilters"
                        :filter-method="defaultValueFilterMethod"
                        :show-overflow-tooltip="true"
                        width="100"
                    />
                    <el-table-column
                        prop="description" v-if="tableShowColumns.description"
                        label="取值范围/枚举"
                        sortable
                        :filters="descriptionFilters"
                        :filter-method="descriptionFilterMethod"
                        width="180"
                        :show-overflow-tooltip="true"
                    >
                      <template slot-scope="scope">
                        {{ buildTableDescriptionColumnValue(scope.row) }}
                        <div v-if="scope.row.refModelDesc != '枚举'">
                          {{ scope.row.description || "" }}
                        </div>

                        <div
                            v-if="scope.row.refModelDesc == '枚举'"
                            v-for="cc in scope.row.description2"
                            :key="cc.value"
                        >
                          <el-link class="link-enum-item" size="mini" type="success">
                            {{ cc.value + "=" + cc.name }}
                          </el-link>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-main>

          <el-footer class="footer-info">
            <el-tag size="small">
              {{
                `数据字典作者: QQ:209308043, 微信:yongyourj, 欢迎私活联系! 版本: ${
                    (agg && agg.ncVersion) || ""
                }, Group: ${(agg && agg.groupName) || ""}, 项目: ${
                    (agg && agg.projectName) || ""
                } 左侧树按住左键拖动可以调整宽度哦 `
              }}
            </el-tag>
            <el-tag size="small" type="danger">{{
                `总计实体数: ${this.totalClassVONum}`
              }}
            </el-tag>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "DataDictionary",
  props: {
    msg: String,
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;

      let ps = this.agg.classMap[data.id] && this.agg.classMap[data.id].perperties;
      let v = "";

      for (let i = 0; i < this.treeSearchPlaces.length; i++) {
        if (this.treeSearchPlaces[i] == 1) {
          v += data.displayname || '';
        } else if (this.treeSearchPlaces[i] == 2) {
          v += data.name || '';
        } else if (this.treeSearchPlaces[i] == 3) {
          v += data.defaultTableName || '';
        } else if (this.treeSearchPlaces[i] == 4 && data.fullClassName) {
          v += data.fullClassName || '';
        } else if (this.treeSearchPlaces[i] == 5 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].pk_billtypecode || '';
        } else if (this.treeSearchPlaces[i] == 6 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].billtypename || '';
        } else if (this.treeSearchPlaces[i] == 7 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].nodecode || '';
        } else if (this.treeSearchPlaces[i] == 8) {
          v += ps ? JSON.stringify(ps) : "";
        } else if (this.treeSearchPlaces[i] == 9 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].fun_name || '';
        } else if (this.treeSearchPlaces[i] == 10 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].paramvalue || '';
        } else if (this.treeSearchPlaces[i] == 11 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].pagecode || '';
        } else if (this.treeSearchPlaces[i] == 12 && this.agg.classMap[data.id]) {
          v += this.agg.classMap[data.id].pageurl || '';
        }
      }

      if (this.searchAutoLower) {
        v = v.toLowerCase();
        value = value.toLowerCase();
      }

      let vs = value.trim()
          .replaceAll('\t', ' ')
          .replaceAll('\n', ' ')
          .replaceAll('\r', ' ')
          .split(' ');

      let i = 0;
      let m = 0;
      for (let s of vs) {
        if (!s) {
          continue
        }
        ++i;

        if (v.indexOf(s) != -1) {
          ++m;
        }
      }

      return i == m && m != 0;
    },
    /**
     * 属性表格 点击了 跳转某个 关联class
     * @param row
     */
    gotoClass(row, type) {
      console.log("gotoClass...", row, type);

      let id = row.id;
      if (type == 1) {
        id = row.dataType;
      }

      if (this.tabsList.map((m) => m.name).indexOf(id) > -1) {
        this.showTabsValue = id;
        return;
      }

      if (type != 4 && type != 5) {
        this.historyClassIds.push(id);
        this.historyClassIdsIndex = this.historyClassIds.length - 1;
      }

      if (!id || !this.agg.classMap || !this.agg.classMap[id]) {
        this.$message.error(
            "找不到你要显示的实体哦! " +
            (id + " " + " " + row && (row.displayname || row.refModelDesc))
        );
        return;
      }

      let c = this.agg.classMap[id];

      let nowClass = c;
      let nowCompoment = this.agg.compomentIdMap[c.componentID];
      let tableData = [];

      console.log("要显示实体啦: ", c, this.nowCompoment);

      if (!c.perperties || c.perperties.length < 1) {
        return;
      }

      this.nameFilters = [];
      this.displayNameFilters = [];
      this.fieldNameFilters = [];
      this.fileTypeFilters = [];
      this.nullableFilters = [];
      this.refModelDescFilters = [];
      this.defaultValueFilters = [];
      this.descriptionFilters = [];

      for (let i = 0; i < c.perperties.length; i++) {
        let r = c.perperties[i];
        tableData.push({
          ...r,
        });

        if (
            this.nameFilters.findIndex((value, index) => {
              return value.value == r.name;
            }) < 0
        ) {
          this.nameFilters.push({
            text: r.name,
            value: r.name,
          });
        }
        if (
            this.displayNameFilters.findIndex((value, index) => {
              return value.value == r.displayName;
            }) < 0
        ) {
          this.displayNameFilters.push({
            text: r.displayName,
            value: r.displayName,
          });
        }
        if (
            this.fieldNameFilters.findIndex((value, index) => {
              return value.value == r.fieldName;
            }) < 0
        ) {
          this.fieldNameFilters.push({
            text: r.fieldName,
            value: r.fieldName,
          });
        }
        if (
            this.fileTypeFilters.findIndex((value, index) => {
              return value.value == r.fileType;
            }) < 0
        ) {
          this.fileTypeFilters.push({
            text: r.fileType,
            value: r.fileType,
          });
        }
        if (
            this.fileTypeDescFilters.findIndex((value, index) => {
              return value.value == r.fileTypeDesc;
            }) < 0
        ) {
          this.fileTypeDescFilters.push({
            text: r.fileTypeDesc,
            value: r.fileTypeDesc,
          });
        }
        if (
            this.nullableFilters.findIndex((value, index) => {
              return value.value == r.nullable;
            }) < 0
        ) {
          this.nullableFilters.push({
            text: r.nullable,
            value: r.nullable,
          });
        }
        if (
            this.refModelDescFilters.findIndex((value, index) => {
              return value.value == r.refModelDesc;
            }) < 0
        ) {
          this.refModelDescFilters.push({
            text: r.refModelDesc,
            value: r.refModelDesc,
          });
        }
        if (
            this.defaultValueFilters.findIndex((value, index) => {
              return value.value == r.defaultValue;
            }) < 0
        ) {
          this.defaultValueFilters.push({
            text: r.defaultValue,
            value: r.defaultValue,
          });
        }
        if (
            this.descriptionFilters.findIndex((value, index) => {
              return value.value == r.description;
            }) < 0
        ) {
          this.descriptionFilters.push({
            text: r.description,
            value: r.description,
          });
        }
      }

      this.tabsList.push({
        title: this.tabTitleJustShowDisplayname
            ? c.displayName
            : `${c.displayName} ${c.name} ${c.defaultTableName}`,
        name: id,
        nowClass,
        nowCompoment,
        tableData, // 实体属性表格数据
      });
      this.showTabsValue = id;

      if (this.notShowCalculation) {
        this.tabsList[this.showTabsValue].tableData = this.tabsList[this.showTabsValue].tableData.filter(
            r => !!!r.calculation
        );
      }
      if (this.notShowDynamic) {
        this.tabsList[this.showTabsValue].tableData = this.tabsList[this.showTabsValue].tableData.filter(
            r => !!!r.dynamic
        );
      }
    },
    handleTreeNodeClick(data, node, treeNode, e) {
      console.log("handleTreeNodeClick...", this.agg, data, node, treeNode, e);
      if (!data || data.type == 0) {
        return;
      }

      this.gotoClass(data, 3);
    },
    /**
     * 后退
     */
    gotoBack() {
      console.log("gotoBack...");
      if (!this.historyClassIds || this.historyClassIds.length < 1) {
        return;
      }

      if (this.historyClassIdsIndex - 1 < 0) {
        this.historyClassIdsIndex = this.historyClassIds.length - 1;
      } else {
        --this.historyClassIdsIndex;
      }

      this.gotoClass({id: this.historyClassIds[this.historyClassIdsIndex]}, 4);
    },
    /**
     * 前进
     */
    gotoForward() {
      console.log("gotoForward...");
      if (!this.historyClassIds || this.historyClassIds.length < 1) {
        return;
      }

      if (this.historyClassIdsIndex + 1 >= this.historyClassIds.length) {
        this.historyClassIdsIndex = this.historyClassIds.length - 1;
      } else {
        ++this.historyClassIdsIndex;
      }

      this.gotoClass({id: this.historyClassIds[this.historyClassIdsIndex]}, 5);
    },
    tableColumnFilter(attr, value, row) {
      console.log("tableColumnFilter...", attr, value, row);

      return row && attr && row[attr] && (row[attr] + "").indexOf(value) > -1;
    },
    expandByOpenTabs() {
      this.treeDefaultExpandedKeys = this.tabsList.map((t) => t.name);
    },
    switchExpandAll() {
      this.treeExpanded = !this.treeExpanded;
      console.log("switchExpandAll...", this.treeExpanded);
      this.$refs.tree.store.defaultExpandAll = this.treeExpanded;
    },
    nameFilterMethod(value, row) {
      return this.tableColumnFilter("name", value, row);
    },
    displayNameFilterMethod(value, row) {
      return this.tableColumnFilter("displayName", value, row);
    },
    fieldNameFilterMethod(value, row) {
      return this.tableColumnFilter("fieldName", value, row);
    },
    fileTypeFilterMethod(value, row) {
      return this.tableColumnFilter("fileType", value, row);
    },
    fileTypeDescFilterMethod(value, row) {
      return this.tableColumnFilter("fileTypeDesc", value, row);
    },
    nullableFilterMethod(value, row) {
      return this.tableColumnFilter("nullable", value, row);
    },
    refModelDescFilterMethod(value, row) {
      return this.tableColumnFilter("refModelDesc", value, row);
    },
    defaultValueFilterMethod(value, row) {
      return this.tableColumnFilter("defaultValue", value, row);
    },
    descriptionFilterMethod(value, row) {
      return this.tableColumnFilter("description", value, row);
    },
    tableRowClassName({row, rowIndex}) {
      if (row.isKey == true) {
        return "table-row-pk";
      }
      return "";
    },
    buildTableDescriptionColumnValue(row) {
      if (!row.description || row.refModelDesc != "枚举" || row.description2) {
        return "";
      }
      row.description2 = row.description && JSON.parse(row.description);
      row.description2 = row.description2 || [];
      return "";
    },
    buildTableRefModelDescColumnValue(row) {
      if (row.refModelDesc == "枚举") {
        return row.refModelDesc + "-" + row.dataType;
      }

      let mdclass = this.agg.classMap[row.dataType];
      if (mdclass) {
        return mdclass.displayName + ' ' + mdclass.defaultTableName;
      }

      return row.refModelDesc;
    },
    handleTabsEdit(targetName, action) {
      console.log("handleTabsEdit...", targetName, action);

      if (action === "remove") {
        let tabs = this.tabsList;
        let activeName = this.showTabsValue;
        if (activeName == targetName) {
          tabs.forEach((tab, index) => {
            if (tab.name == targetName) {
              let nextTab = tabs[index + 1] || tabs[index - 1];
              if (nextTab) {
                activeName = nextTab.name;
              }
            }
          });
        }

        this.showTabsValue = activeName;
        this.tabsList = tabs.filter((tab) => tab.name != targetName);
      }
    },
    buildTableClassTypeName(item) {
      console.log("buildTableClassTypeName...", item);
      return `${item.nowClass.displayName || "未知"}(${
          item.nowClass.defaultTableName || "未知表名"
      })  (${this.classTypeName[item.nowClass.classType] || "未知类型"})`;
    },
    closeAllTab() {
      this.tabsList = [];
      this.showTabsValue = "";
      this.historyClassIds = [];
      this.historyClassIdsIndex = -1;
    },
  },
  created() {
  },
  destroyed() {
  },
  data() {
    let agg = window.agg;

    return {
      filterText: "",
      agg: agg,
      treeData: agg ? agg.modules : [], //左侧树
      totalClassVONum: agg && agg.classMap ? Object.keys(agg.classMap).length : 0, //实体总计数量
      defaultProps: {
        children: "childs",
        label: (row, node) => {
          return this.treeShowNames
              .map((k) => (k == "type" ? this.treeTypeName[row.type] || "" : row[k]))
              .join(" ");
        },
      },

      showTabsValue: "",
      tabsList: [],

      classTypeName: {
        201: "实体*",
        203: "枚举@",
        206: "业务接口$",
      },
      treeTypeName: {
        201: "*",
        203: "@",
        206: "$",
      },
      historyClassIds: [],
      historyClassIdsIndex: -1,
      nameFilters: [],
      displayNameFilters: [],
      fieldNameFilters: [],
      fileTypeFilters: [],
      nullableFilters: [],
      refModelDescFilters: [],
      defaultValueFilters: [],
      descriptionFilters: [],
      fileTypeDescFilters: [],
      tableShowColumns: {
        name: true,
        displayName: true,
        fieldName: true,
        fileTypeDesc: true,
        nullable: true,
        refModelDesc: true,
        defaultValue: true,
        description: true,
      },
      searchIncloudProperts: true,
      treeShowNames: ["displayname", "defaultTableName"],
      treeShowNamesOptions: [
        {
          value: "type",
          label: "类型",
        },
        {
          value: "name",
          label: "编码",
        },
        {
          value: "displayname",
          label: "名称",
        },
        {
          value: "defaultTableName",
          label: "表名",
        },
        {
          value: "id",
          label: "ID",
        },
      ],
      treeSearchPlaces: [1, 2, 3, 4, 5, 6, 7],
      treeSearchPlacesOptions: [
        {
          value: 1,
          label: "树-名称",
        },
        {
          value: 2,
          label: "树-编码",
        },
        {
          value: 3,
          label: "树-表名",
        },
        {
          value: 4,
          label: "VO类",
        },
        {
          value: 5,
          label: "单据编码",
        },
        {
          value: 6,
          label: "单据名称",
        },
        {
          value: 7,
          label: "节点编码",
        },
        {
          value: 8,
          label: "属性列表",
        },
        {
          value: 9,
          label: "重量端节点名",
        },
        {
          value: 10,
          label: "重量端XML配置",
        },
        {
          value: 11,
          label: "轻量端页码编码",
        },
        {
          value: 12,
          label: "轻量端页码地址",
        },
      ],
      searchAutoLower: true,
      showSettingPanel: false,
      showSimpleClassName: true,
      tabTitleJustShowDisplayname: true,
      showLeftTree: true,
      notShowCalculation: true,
      notShowDynamic: false,
      treeExpanded: false,
      treeDefaultExpandedKeys: [],
    };
  },
};
</script>

<style>
.main-div {
  max-height: calc(100vh - 20px);
}

.props-table {
}

body {
  height: 98%;
}

.main-title-div {
  margin-top: 1px;
  margin-bottom: 10px;
}

.goto-button {
  margin-top: 5px;
  padding-bottom: 5px;
}

.footer-info {
  max-height: 20px;
  margin-top: 5px;
}

.filter-tree {
  max-height: calc(100vh * 0.93);
}

.left-div {
}

.el-tree-node {
}

.props-table-div {
  margin-top: 5px;
}

.el-table .table-row-pk {
  color: red;
}

.left-treeShowNames {
  width: calc(100vh * 0.345);
}

.left-searchPlaces {
  margin-top: 5px;
  width: calc(100vh * 0.345);
}

.left-div-aside-setting {
  width: calc(160vh);
}

.left-searchIncloudProperts {
  margin-top: 5px;
}

.left-filterText {
  margin-top: 5px;
}

.main-tabs {
  margin-top: 5px;
}
</style>
