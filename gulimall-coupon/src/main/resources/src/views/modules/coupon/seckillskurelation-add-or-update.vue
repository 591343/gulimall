<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="?id" prop="promotionId">
      <el-input v-model="dataForm.promotionId" placeholder="?id"></el-input>
    </el-form-item>
    <el-form-item label="?????id" prop="promotionSessionId">
      <el-input v-model="dataForm.promotionSessionId" placeholder="?????id"></el-input>
    </el-form-item>
    <el-form-item label="??Ʒid" prop="skuId">
      <el-input v-model="dataForm.skuId" placeholder="??Ʒid"></el-input>
    </el-form-item>
    <el-form-item label="??ɱ?۸" prop="seckillPrice">
      <el-input v-model="dataForm.seckillPrice" placeholder="??ɱ?۸"></el-input>
    </el-form-item>
    <el-form-item label="??ɱ????" prop="seckillCount">
      <el-input v-model="dataForm.seckillCount" placeholder="??ɱ????"></el-input>
    </el-form-item>
    <el-form-item label="ÿ???޹?????" prop="seckillLimit">
      <el-input v-model="dataForm.seckillLimit" placeholder="ÿ???޹?????"></el-input>
    </el-form-item>
    <el-form-item label="???" prop="seckillSort">
      <el-input v-model="dataForm.seckillSort" placeholder="???"></el-input>
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
          promotionId: '',
          promotionSessionId: '',
          skuId: '',
          seckillPrice: '',
          seckillCount: '',
          seckillLimit: '',
          seckillSort: ''
        },
        dataRule: {
          promotionId: [
            { required: true, message: '?id不能为空', trigger: 'blur' }
          ],
          promotionSessionId: [
            { required: true, message: '?????id不能为空', trigger: 'blur' }
          ],
          skuId: [
            { required: true, message: '??Ʒid不能为空', trigger: 'blur' }
          ],
          seckillPrice: [
            { required: true, message: '??ɱ?۸不能为空', trigger: 'blur' }
          ],
          seckillCount: [
            { required: true, message: '??ɱ????不能为空', trigger: 'blur' }
          ],
          seckillLimit: [
            { required: true, message: 'ÿ???޹?????不能为空', trigger: 'blur' }
          ],
          seckillSort: [
            { required: true, message: '???不能为空', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/coupon/seckillskurelation/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.promotionId = data.seckillSkuRelation.promotionId
                this.dataForm.promotionSessionId = data.seckillSkuRelation.promotionSessionId
                this.dataForm.skuId = data.seckillSkuRelation.skuId
                this.dataForm.seckillPrice = data.seckillSkuRelation.seckillPrice
                this.dataForm.seckillCount = data.seckillSkuRelation.seckillCount
                this.dataForm.seckillLimit = data.seckillSkuRelation.seckillLimit
                this.dataForm.seckillSort = data.seckillSkuRelation.seckillSort
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
              url: this.$http.adornUrl(`/coupon/seckillskurelation/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'promotionId': this.dataForm.promotionId,
                'promotionSessionId': this.dataForm.promotionSessionId,
                'skuId': this.dataForm.skuId,
                'seckillPrice': this.dataForm.seckillPrice,
                'seckillCount': this.dataForm.seckillCount,
                'seckillLimit': this.dataForm.seckillLimit,
                'seckillSort': this.dataForm.seckillSort
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
