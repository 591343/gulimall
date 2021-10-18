<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="?Ż?ȯid" prop="couponId">
      <el-input v-model="dataForm.couponId" placeholder="?Ż?ȯid"></el-input>
    </el-form-item>
    <el-form-item label="??Աid" prop="memberId">
      <el-input v-model="dataForm.memberId" placeholder="??Աid"></el-input>
    </el-form-item>
    <el-form-item label="??Ա???" prop="memberNickName">
      <el-input v-model="dataForm.memberNickName" placeholder="??Ա???"></el-input>
    </el-form-item>
    <el-form-item label="??ȡ??ʽ[0->??̨???ͣ?1->??????ȡ]" prop="getType">
      <el-input v-model="dataForm.getType" placeholder="??ȡ??ʽ[0->??̨???ͣ?1->??????ȡ]"></el-input>
    </el-form-item>
    <el-form-item label="????ʱ?" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="????ʱ?"></el-input>
    </el-form-item>
    <el-form-item label="ʹ??״̬[0->δʹ?ã?1->??ʹ?ã?2->?ѹ???]" prop="useType">
      <el-input v-model="dataForm.useType" placeholder="ʹ??״̬[0->δʹ?ã?1->??ʹ?ã?2->?ѹ???]"></el-input>
    </el-form-item>
    <el-form-item label="ʹ??ʱ?" prop="useTime">
      <el-input v-model="dataForm.useTime" placeholder="ʹ??ʱ?"></el-input>
    </el-form-item>
    <el-form-item label="????id" prop="orderId">
      <el-input v-model="dataForm.orderId" placeholder="????id"></el-input>
    </el-form-item>
    <el-form-item label="?????" prop="orderSn">
      <el-input v-model="dataForm.orderSn" placeholder="?????"></el-input>
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
          couponId: '',
          memberId: '',
          memberNickName: '',
          getType: '',
          createTime: '',
          useType: '',
          useTime: '',
          orderId: '',
          orderSn: ''
        },
        dataRule: {
          couponId: [
            { required: true, message: '?Ż?ȯid不能为空', trigger: 'blur' }
          ],
          memberId: [
            { required: true, message: '??Աid不能为空', trigger: 'blur' }
          ],
          memberNickName: [
            { required: true, message: '??Ա???不能为空', trigger: 'blur' }
          ],
          getType: [
            { required: true, message: '??ȡ??ʽ[0->??̨???ͣ?1->??????ȡ]不能为空', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: '????ʱ?不能为空', trigger: 'blur' }
          ],
          useType: [
            { required: true, message: 'ʹ??״̬[0->δʹ?ã?1->??ʹ?ã?2->?ѹ???]不能为空', trigger: 'blur' }
          ],
          useTime: [
            { required: true, message: 'ʹ??ʱ?不能为空', trigger: 'blur' }
          ],
          orderId: [
            { required: true, message: '????id不能为空', trigger: 'blur' }
          ],
          orderSn: [
            { required: true, message: '?????不能为空', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/coupon/couponhistory/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.couponId = data.couponHistory.couponId
                this.dataForm.memberId = data.couponHistory.memberId
                this.dataForm.memberNickName = data.couponHistory.memberNickName
                this.dataForm.getType = data.couponHistory.getType
                this.dataForm.createTime = data.couponHistory.createTime
                this.dataForm.useType = data.couponHistory.useType
                this.dataForm.useTime = data.couponHistory.useTime
                this.dataForm.orderId = data.couponHistory.orderId
                this.dataForm.orderSn = data.couponHistory.orderSn
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
              url: this.$http.adornUrl(`/coupon/couponhistory/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'couponId': this.dataForm.couponId,
                'memberId': this.dataForm.memberId,
                'memberNickName': this.dataForm.memberNickName,
                'getType': this.dataForm.getType,
                'createTime': this.dataForm.createTime,
                'useType': this.dataForm.useType,
                'useTime': this.dataForm.useTime,
                'orderId': this.dataForm.orderId,
                'orderSn': this.dataForm.orderSn
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
