<template>
  <div class="top">
    <el-container>
      <el-container>
        <el-aside class="left-div">
          <el-input clearable resize="both"
                    v-model="filterText"
                    placeholder="搜索"
                    class="w-50 m-2"
                    size="small"
                    :suffix-icon="Search"/>
          <el-tree
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
              <el-button v-if="nowClass && nowClass.displayName" link type="primary">{{ nowClass.displayName }}
              </el-button>
              <el-button v-if="nowClass && nowClass.displayName" link type="success">{{
                  nowClass.defaultTableName
                }}
              </el-button>
              <el-button v-if="nowClass && nowClass.displayName" link type="info">{{
                  nowClass.fullClassName
                }}
              </el-button>
              <el-button v-if="nowClass && nowClass.aggFullClassName" link type="info">{{
                  `Agg类: ${nowClass.aggFullClassName}`
                }}
              </el-button>

              <div v-if="nowCompoment && nowCompoment.classDTOS">
                <el-button link
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

            <el-table class="props-table" border stripe
                      ref="filterTable"
                      :data="tableData"
                      style="width: 100%">
              <el-table-column type="index"/>
              <el-table-column
                  prop="name"
                  label="属性编码"
                  sortable
                  width="180"
                  column-key="displayname"
              />
              <el-table-column
                  prop="displayname"
                  label="属性名称" sortable
                  width="180"/>
              <el-table-column
                  prop="fieldName"
                  label="字段编码" sortable
                  width="180"/>
              <el-table-column
                  prop="fileTypeDesc"
                  label="字段类型" sortable
                  width="180"/>
              <el-table-column
                  prop="nullable"
                  label="可空" sortable
                  width="80"/>
              <el-table-column
                  prop="refModelDesc"
                  label="引用模型" sortable
                  width="180">
                <template #title="scope">
                  <el-button link :type="scope.row.refModelName ? 'primary' : 'info'"
                             :disabled="!scope.row.refModelName" v-on:click="gotoClass(scope.row, 1)">
                    {{ scope.row.tag }}
                    <el-icon class="el-icon--right">
                      <icon-view/>
                    </el-icon>
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column
                  prop="defaultValue"
                  label="默认值" sortable
                  width="100"/>
              <el-table-column
                  prop="description"
                  label="取值范围/枚举" sortable
                  width="180"/>
            </el-table>
          </el-main>

          <el-footer class="footer-info">
            <el-statistic
                group-separator=","
                :precision="2"
                :value="totalClassVONum"
                title="总计实体数"
            ></el-statistic>
            <el-tag size="small">
              {{
                `数据字典作者 By QQ:209308043微信:yongyourj 欢迎私活联系 版本:${agg && agg.ncVersion || ''} Group:${agg && agg.groupName || ''} 项目:${agg && agg.projectName || ''}`
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

      let v = (data.type == 0 ? '*' : '') + data.name + ' ' + data.displayname;

      return v.indexOf(value) != -1;
    },
    /**
     * 属性表格 点击了 跳转某个 关联class
     * @param row
     */
    gotoClass(row, type) {
      console.log('gotoClass...', row, type);

    },
    handleTreeNodeClick(data, node, treeNode, e) {
      console.log('handleTreeNodeClick...', this.agg, data, node, treeNode, e);
      if (!data || data.type == 0) {
        return;
      }

      if (!data.id || !this.agg.classMap || !this.agg.classMap[data.id]) {
        this.$message.error('找不到你要显示的实体哦! ' + (data && data.id + ' ' + data.displayname))
        return;
      }

      let c = this.agg.classMap[data.id];
      console.log('要显示实体啦: ', c);


    }
  },
  data() {
    let agg = window.agg;

    return {
      filterText: '',
      agg: agg,
      treeData: agg ? agg.modules : [], //左侧树
      totalClassVONum: agg ? agg.classMap.length : 0, //实体总计数量
      defaultProps: {
        children: 'childs',
        label: (row, node) => {
          return (row.type == 0 ? '*' : '') + row.name + ' ' + row.displayname;
        }
      },
      nowClass: null,
      nowCompoment: null,
      tableData: [ // 实体属性表格数据
        {
          date: '2016-05-02',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1518 弄',
          tag: '家'
        }, {
          date: '2016-05-04',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1517 弄',
          tag: '公司'
        }, {
          date: '2016-05-01',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1519 弄',
          tag: '家'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄',
          tag: '公司'
        }
      ]
    };
  }
}
</script>

<style scoped>
.main-div {
  height: calc(100vh - 20px);
}

.props-table {
}

body {
  height: 98%;
}

.left-div {
  width: calc(100vh * 0.8);
}

.main-title-div {
  height: 50px;
  margin-top: 1px;
  margin-bottom: 10px;
}

.footer-info {
  margin-top: 20px;
}
</style>