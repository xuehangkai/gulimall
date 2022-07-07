<template>
  <div>
    <el-switch
      v-model="draggable"
      active-text="开启拖拽"
      inactive-text="关闭拖拽"
    >
    </el-switch>
    <el-button v-if="draggable" @click="batchSave">批量保存</el-button>
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <el-tree
      :data="menus"
      :props="defaultProps"
      show-checkbox
      :expand-on-click-node="false"
      node-key="catId"
      :default-expanded-keys="expandedkey"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
      ref="menuTree"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            添加
          </el-button>

          <el-button type="text" size="mini" @click="edit(data)">
            修改
          </el-button>

          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            删除
          </el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  //import 引入的组件需要注入到对象中才能使用
  components: {},

  props: {},

  data() {
    //这里存数据
    return {
      pid: 0,
      draggable: false,
      updateNodes: [],
      maxLevel: 0,
      title: "",
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        productUnit: "",
        icon: "",
        catId: null,
      },
      menus: [],
      expandedkey: [],
      defaultProps: {
        childrean: "childrean",
        label: "name",
      },
      dialogVisible: false,
    };
  },

  //计算属性
  computed: {},

  //监控data中数据变化
  watch: {},

  //方法
  methods: {
    batchDelete() {
      let catIds = [];
      let checkedNodes = this.$refs.menuTree.getCheckedNodes();
      console.log(checkedNodes);
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId);
      }
      this.$confirm("是否批量删除菜单, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(catIds, false),
          })
            .then(({ data }) => {
              this.$message({
                type: "success",
                message: "菜单批量删除成功",
              });
              this.getMenus();
            })
            .catch(({ data }) => {
              this.$message({
                type: "info",
                message: "菜单批量删除失败",
              });
            });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消批量删除",
          });
        });
    },
    batchSave() {
      //console.log(this.updateNodes);
      console.log(this.pid);
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false),
      })
        .then(({ data }) => {
          this.$message({
            type: "success",
            message: "菜单修改成功",
          });
          this.getMenus();
          this.expandedkey = [this.pid];
          this.updateNodes = [];
          this.pid = 0;
        })
        .catch(({ data }) => {
          this.$message({
            type: "info",
            message: "菜单修改失败",
          });
        });
    },
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      }).then(({ data }) => {
        // console.log(data.data);
        this.menus = data.data;
      });
    },
    handleDrop(draggingNode, dropNode, type, event) {
      //console.log(draggingNode, dropNode, type, event);
      let pid = 0;
      let siblings = null;
      if (type == "before" || type == "after") {
        pid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        siblings = dropNode.parent.childNodes;
      } else {
        pid = dropNode.data.catId;
        siblings = dropNode.childNodes;
      }
      this.pid = pid;
      //console.log(type);
      //console.log(this.pid);
      for (let i = 0; i < siblings.length; i++) {
        //console.log({ catId: siblings[i].data.catId, sort: i });
        if (siblings[i].data.catId == draggingNode.data.catId) {
          let catLevel = draggingNode.level;
          if (siblings[i].level != draggingNode.level) {
            catLevel = siblings[i].level;
            this.updateChildNode(siblings[i]);
          }
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pid,
          });
        } else {
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }
      //console.log(this.updateNodes);
    },
    updateChildNode(node) {
      if (node.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          this.updateNodes.push({
            catId: node.childNodes[i].data.catId,
            catLevel: node.childNodes[i].level,
          });
          this.updateChildNode(node.childNodes[i]);
        }
      }
    },
    allowDrop(draggingNode, dropNode, type) {
      this.maxLevel = 0;
      this.countNodelevel(draggingNode);
      let deep = 0;
      deep = this.maxLevel - draggingNode.data.catLevel + 1;
      if (type == "inner") {
        return deep + dropNode.level <= 3;
      } else {
        return deep + dropNode.parent.level <= 3;
      }
    },
    countNodelevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].data.catLevel > this.maxLevel) {
            this.maxLevel = node.childNodes[i].data.catLevel;
          }
          this.countNodelevel(node.childNodes[i]);
        }
      } else {
        this.maxLevel = node.data.catLevel;
      }
    },
    handleNodeClick(data) {
      console.log(data);
    },

    submit() {
      if (this.title == "添加菜单") {
        this.addCatepory();
      }
      if (this.title == "修改菜单") {
        this.editCategory();
      }
    },

    edit(data) {
      this.title = "修改菜单";
      this.dialogVisible = true;

      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get",
      }).then(({ data }) => {
        this.category.name = data.category.name;
        this.category.catId = data.category.catId;
        this.category.icon = data.category.icon;
        this.category.productUnit = data.category.productUnit;
      });
    },

    editCategory() {
      var { catId, name, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ catId, name, icon, productUnit }, false),
      })
        .then(({ data }) => {
          this.$message({
            type: "success",
            message: "菜单修改成功",
          });
          this.dialogVisible = false;
          this.getMenus();
          this.expandedkey = [catId];
          this.category.name = "";
          this.category.catId = null;
          this.category.icon = "";
          this.category.productUnit = "";
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "菜单修改失败",
          });
        });
    },

    append(data) {
      this.title = "添加菜单";
      this.dialogVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
    },
    addCatepory() {
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      })
        .then(({ data }) => {
          this.$message({
            type: "success",
            message: "菜单添加成功",
          });
          this.dialogVisible = false;
          this.getMenus();
          this.expandedkey = [this.category.parentCid];
          this.category.name = "";
          this.category.parentCid = 0;
          this.category.catLevel = 0;
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "菜单添加失败",
          });
        });
    },

    remove(node, data) {
      var ids = [data.catId];

      this.$confirm(`是否删除【${data.name}】菜单, 是否继续?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            this.getMenus();
            this.expandedkey = [node.parent.data.catId];
            this.$message({
              type: "success",
              message: "删除成功",
            });
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
  },

  //声明周期 - 创建完成（可以访问当前this实例）
  created() {
    this.getMenus();
  },

  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},

  beforeCreate() {}, //生命周期 - 创建之前

  beforeMount() {}, //生命周期 - 挂载之前

  updated() {}, //生命周期 - 更新之后

  beforeDestroy() {}, //生命周期 - 销毁之前

  destroyed() {}, //生命周期 - 销毁之后

  activated() {}, //缓存keep-alive
};
</script>
    <style scoped>
</style>