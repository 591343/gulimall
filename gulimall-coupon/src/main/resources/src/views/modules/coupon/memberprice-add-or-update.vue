<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="sku_id" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="sku_id"></el-input>
    </el-form-item>
    <el-form-item label="??Ա?ȼ?id" prop="memberLevelId">
      <el-input v-model="dataForm.memberLevelId" placeholder="??Ա?ȼ?id"></el-input>
    </el-form-item>
    <el-form-item label="??Ա?ȼ???" prop="memberLevelName">
      <el-input v-model="dataForm.memberLevelName" placeholder="??Ա?ȼ???"></el-input>
    </el-form-item>
    <el-form-item label="??Ա??Ӧ?۸" prop="memberPrice">
      <el-input v-model="dataForm.memberPrice" placeholder="??Ա??Ӧ?۸"></el-input>
    </el-form-item>
    <el-form-item label="?ɷ??????????Ż?[0-???ɵ????Żݣ?1-?ɵ???]" prop="addOther">
      <el-input v-model="dataForm.addOther" placeholder="?ɷ??????????Ż?[0-???ɵ????Żݣ?1-?ɵ???]"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          skuId: '',
          memberLevelId: '',
          memberLevelName: '',
          memberPrice: '',
          addOther: ''
        },
        dataRule: {
          skuId: [
            { required: true, message: 'sku_id不能为空', trigger: 'blur' }
          ],
          memberLevelId: [
            { required: true, message: '??Ա?ȼ?id不能为空', trigger: 'blur' }
          ],
          memberLevelName: [
            { required: true, message: '??Ա?ȼ???不能为空', trigger: 'blur' }
          ],
          memberPrice: [
            { required: true, message: '??Ա??Ӧ?۸不能为空', trigger: 'blur' }
          ],
          addOther: [
            { required: true, message: '?ɷ??????????Ż?[0-???ɵ????Żݣ?1-?ɵ???]不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/coupon/memberprice/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.skuId = data.memberPrice.skuId
                this.dataForm.memberLevelId = data.memberPrice.memberLevelId
                this.dataForm.memberLevelName = data.memberPrice.memberLevelName
                this.dataForm.memberPrice = data.memberPrice.memberPrice
                this.dataForm.addOther = data.memberPrice.addOther
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/coupon/memberprice/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'skuId': this.dataForm.skuId,
                'memberLevelId': this.dataForm.memberLevelId,
                'memberLevelName': this.dataForm.memberLevelName,
                'memberPrice': this.dataForm.memberPrice,
                'addOther': this.dataForm.addOther
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
