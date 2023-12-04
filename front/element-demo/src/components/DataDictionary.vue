<template>
  <div class="top">
    <el-container>
      <el-container>
        <el-aside class="left-div-aside" v-drag>
          <el-input clearable resize="both"
                    v-model="filterText"
                    placeholder="搜索"
                    class="w-50 m-2"
                    size="small"
                    :suffix-icon="Search"/>
          <el-tree v-drag
                   class="filter-tree"
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
          <el-main>
            <div class="main-title-div">
              <el-button link size="mini"
                         type="info"
                         v-on:click="gotoBack"
              >后退
              </el-button>
              <el-button link size="mini"
                         type="info"
                         v-on:click="gotoForward"
              >前进
              </el-button>

              <el-link v-if="nowClass && nowClass.displayName" link type="primary">
                {{ `${nowClass.displayName || '未知'} (${this.classTypeName[nowClass.classType] || '未知类型'})` }}
              </el-link>
              <el-link v-if="nowClass && nowClass.displayName" type="success">{{
                  `| 表: ${nowClass.defaultTableName || '未知'}`
                }}
              </el-link>
              <el-link v-if="nowClass && nowClass.displayName" link type="info">{{
                  `| VO类: ${nowClass.fullClassName || '未知'}`
                }}
              </el-link>
              <el-link v-if="nowClass && nowClass.aggFullClassName" link type="info">{{
                  `| Agg类: ${nowClass.aggFullClassName || '未知'}`
                }}
              </el-link>

              <br/>

              <div v-if="nowCompoment && nowCompoment.classDTOS" class="goto-button-div">
                <el-button class="goto-button" link size="mini"
                           type="danger"
                           v-for="cc in nowCompoment.classDTOS"
                           :key="cc.id"
                           v-on:click="gotoClass(cc, 2)"
                >
                  {{
                    cc.name + ' ' + cc.displayName + ' ' + cc.fullClassName.substr(cc.fullClassName.lastIndexOf('.') + 1)
                  }}
                </el-button>
              </div>
            </div>

            <div class="props-table-div">
              <el-table class="props-table" border stripe
                        ref="filterTable"  :highlight-current-row="true"
                        :data="tableData" :row-class-name="tableRowClassName"
                        style="width: 100%"
              >
                <el-table-column type="index"/>
                <el-table-column
                    prop="name"
                    label="属性编码"
                    sortable
                    width="180"
                    column-key="name"
                    :filters="nameFilters"
                    :filter-method="nameFilterMethod"
                />
                <el-table-column
                    prop="displayName"
                    label="属性名称" sortable
                    :filters="displayNameFilters"
                    :filter-method="displayNameFilterMethod"
                    width="180"/>
                <el-table-column
                    prop="fieldName"
                    label="字段编码" sortable
                    :filters="fieldNameFilters"
                    :filter-method="fieldNameFilterMethod"
                    width="180"/>
                <el-table-column
                    prop="fileTypeDesc"
                    label="字段类型" sortable
                    :filters="fileTypeDescFilters"
                    :filter-method="fileTypeDescFilterMethod"
                    width="180"/>
                <el-table-column
                    prop="nullable"
                    label="可空" sortable
                    :filters="nullableFilters"
                    :filter-method="nullableFilterMethod"
                    width="90"/>
                <el-table-column
                    prop="refModelDesc"
                    label="引用模型" sortable
                    :filters="refModelDescFilters"
                    :filter-method="refModelDescFilterMethod"
                    width="280">
                  <template slot-scope="scope">
                    <el-link link :type="scope.row.refModelName ? 'primary' : 'info'"
                             :disabled="!scope.row.refModelName" v-on:click="gotoClass(scope.row, 1)">
                      {{
                        scope.row.refModelDesc == '枚举' ? scope.row.refModelDesc + '-' + scope.row.dataType : scope.row.refModelDesc
                      }}
                      <el-icon class="el-icon--right">
                        <icon-view/>
                      </el-icon>
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column
                    prop="defaultValue"
                    label="默认值" sortable
                    :filters="defaultValueFilters"
                    :filter-method="defaultValueFilterMethod"
                    width="100"/>
                <el-table-column
                    prop="description"
                    label="取值范围/枚举" sortable
                    :filters="descriptionFilters"
                    :filter-method="descriptionFilterMethod"
                    width="180">
                  <template slot-scope="scope">
                    {{ buildTableDescriptionColumnValue(scope.row) }}
                    <div v-if="scope.row.refModelDesc != '枚举'">{{scope.row.description || ''}}</div>

                    <div v-if="scope.row.refModelDesc == '枚举'"
                         v-for="cc in scope.row.description2"
                         :key="cc.value" >
                      <el-link
                               class="link-enum-item"
                               size="mini"
                               type="success"
                      >
                        {{ cc.value + '=' + cc.name }}
                      </el-link>
                    </div>

                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-main>

          <el-footer class="footer-info">
            <el-tag size="small">
              {{
                `数据字典作者: QQ:209308043, 微信:yongyourj, 欢迎私活联系! 版本: ${agg && agg.ncVersion || ''}, Group: ${agg && agg.groupName || ''}, 项目: ${agg && agg.projectName || ''} 左侧树按住左键拖动可以调整宽度哦 `
              }}
            </el-tag>
            <el-tag size="small" type="danger">{{ `总计实体数: ${this.totalClassVONum}` }}</el-tag>
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
    msg: String
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;

      let ps = this.agg.classMap[data.id] && this.agg.classMap[data.id].perperties
      let v = JSON.stringify(data) + (ps ? JSON.stringify(ps) : '');

      return v.toLowerCase().indexOf(value.toLowerCase()) != -1;
    },
    /**
     * 属性表格 点击了 跳转某个 关联class
     * @param row
     */
    gotoClass(row, type) {
      console.log('gotoClass...', row, type);

      let id = row.id;
      if (type == 1) {
        id = row.dataType;
      }

      if (type != 4 && type != 5) {
        this.historyClassIds.push(id)
        this.historyClassIdsIndex = this.historyClassIds.length - 1
      }

      if (!id || !this.agg.classMap || !this.agg.classMap[id]) {
        this.$message.error('找不到你要显示的实体哦! ' + (id + ' ' + ' ' + row && (row.displayname || row.refModelDesc)))
        return;
      }

      let c = this.agg.classMap[id];

      this.nowClass = c;
      this.nowCompoment = this.agg.compomentIdMap[c.componentID];
      this.tableData = [];

      console.log('要显示实体啦: ', c, this.nowCompoment);

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
        this.tableData.push({
          ...r
        });

        if (this.nameFilters.findIndex((value, index) => {
          return value.value == r.name;
        }) < 0) {
          this.nameFilters.push({
            text: r.name,
            value: r.name
          })
        }
        if (this.displayNameFilters.findIndex((value, index) => {
          return value.value == r.displayName;
        }) < 0) {
          this.displayNameFilters.push({
            text: r.displayName,
            value: r.displayName
          })
        }
        if (this.fieldNameFilters.findIndex((value, index) => {
          return value.value == r.fieldName;
        }) < 0) {
          this.fieldNameFilters.push({
            text: r.fieldName,
            value: r.fieldName
          })
        }
        if (this.fileTypeFilters.findIndex((value, index) => {
          return value.value == r.fileType;
        }) < 0) {
          this.fileTypeFilters.push({
            text: r.fileType,
            value: r.fileType
          })
        }
        if (this.fileTypeDescFilters.findIndex((value, index) => {
          return value.value == r.fileTypeDesc;
        }) < 0) {
          this.fileTypeDescFilters.push({
            text: r.fileTypeDesc,
            value: r.fileTypeDesc
          })
        }
        if (this.nullableFilters.findIndex((value, index) => {
          return value.value == r.nullable;
        }) < 0) {
          this.nullableFilters.push({
            text: r.nullable,
            value: r.nullable
          })
        }
        if (this.refModelDescFilters.findIndex((value, index) => {
          return value.value == r.refModelDesc;
        }) < 0) {
          this.refModelDescFilters.push({
            text: r.refModelDesc,
            value: r.refModelDesc
          })
        }
        if (this.defaultValueFilters.findIndex((value, index) => {
          return value.value == r.defaultValue;
        }) < 0) {
          this.defaultValueFilters.push({
            text: r.defaultValue,
            value: r.defaultValue
          })
        }
        if (this.descriptionFilters.findIndex((value, index) => {
          return value.value == r.description;
        }) < 0) {
          this.descriptionFilters.push({
            text: r.description,
            value: r.description
          })
        }

      }
    },
    handleTreeNodeClick(data, node, treeNode, e) {
      console.log('handleTreeNodeClick...', this.agg, data, node, treeNode, e);
      if (!data || data.type == 0) {
        return;
      }

      this.gotoClass(data, 3)
    },
    /**
     * 后退
     */
    gotoBack() {
      console.log('gotoBack...');
      if (!this.historyClassIds || this.historyClassIds.length < 1) {
        return;
      }

      if (this.historyClassIdsIndex - 1 < 0) {
        this.historyClassIdsIndex = this.historyClassIds.length - 1
      } else {
        --this.historyClassIdsIndex
      }

      this.gotoClass({id: this.historyClassIds[this.historyClassIdsIndex]}, 4)
    },
    /**
     * 前进
     */
    gotoForward() {
      console.log('gotoForward...');
      if (!this.historyClassIds || this.historyClassIds.length < 1) {
        return;
      }

      if (this.historyClassIdsIndex + 1 >= this.historyClassIds.length) {
        this.historyClassIdsIndex = this.historyClassIds.length - 1
      } else {
        ++this.historyClassIdsIndex
      }

      this.gotoClass({id: this.historyClassIds[this.historyClassIdsIndex]}, 5)
    },
    tableColumnFilter(attr, value, row) {
      console.log('tableColumnFilter...', attr, value, row);

      return row && attr && row[attr] && (row[attr] + '').indexOf(value) > -1;
    },
    nameFilterMethod(value, row) {
      return this.tableColumnFilter('name', value, row);
    },
    displayNameFilterMethod(value, row) {
      return this.tableColumnFilter('displayName', value, row);
    },
    fieldNameFilterMethod(value, row) {
      return this.tableColumnFilter('fieldName', value, row);
    },
    fileTypeFilterMethod(value, row) {
      return this.tableColumnFilter('fileType', value, row);
    },
    fileTypeDescFilterMethod(value, row) {
      return this.tableColumnFilter('fileTypeDesc', value, row);
    },
    nullableFilterMethod(value, row) {
      return this.tableColumnFilter('nullable', value, row);
    },
    refModelDescFilterMethod(value, row) {
      return this.tableColumnFilter('refModelDesc', value, row);
    },
    defaultValueFilterMethod(value, row) {
      return this.tableColumnFilter('defaultValue', value, row);
    },
    descriptionFilterMethod(value, row) {
      return this.tableColumnFilter('description', value, row);
    },
    tableRowClassName({row, rowIndex}) {
      if (row.isKey == true) {
        return 'table-row-pk';
      }
      return '';
    },
    buildTableDescriptionColumnValue(row) {
      if (!row.description || row.refModelDesc != '枚举' || row.description2) {
        return '';
      }
      row.description2 = row.description && JSON.parse(row.description);
      row.description2 = row.description2 || []
      return '';
    },
  },
  created() {
  },
  destroyed() {
  },
  data() {
    let agg = window.agg;

    return {
      filterText: '',
      agg: agg,
      treeData: agg ? agg.modules : [], //左侧树
      totalClassVONum: agg && agg.classMap ? Object.keys(agg.classMap).length : 0, //实体总计数量
      defaultProps: {
        children: 'childs',
        label: (row, node) => {
          return (this.treeTypeName[row.type] || '') + row.displayname + ' ' + row.name + ' ' + (row.defaultTableName || '');
        }
      },
      nowClass: null,
      nowCompoment: null,
      tableData: [],// 实体属性表格数据
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
    };
  }
}
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

</style>