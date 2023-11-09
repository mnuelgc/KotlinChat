package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.TOP

class ChatSpaceView : ScrollView {

    var constraintLayout : ConstraintLayout? = null
    var numsOfDialog : Int = 0
    var lastId : Int = 0

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int)
            : super(context, attrs, defStyle) {initialize()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initialize()}
    constructor(context: Context?) : super(context) {initialize()}

    fun initialize(){
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.chat_space_view, this, true)
        constraintLayout = findViewById(R.id.messageBlackBoard)
    }


    //From 0 to Client 1 from server
    public fun appendDialog(message : String, from: Int, color : Int, userName : String?){
        val dialog = createDialog(message, from)

        constraintLayout?.addView(dialog)

        val set = ConstraintSet()
        set.clone(constraintLayout)
        dialog.id.also { diag ->
            if(from == 1) {
                set.connect(diag, START, PARENT_ID, START)
            }else if (from == 0 ){
                set.connect(diag, END, PARENT_ID, END)
            }
            if(lastId == 0){
                set.connect(diag, TOP, PARENT_ID, TOP)
            }else{
                set.connect(diag, TOP, lastId, BOTTOM)
            }
            lastId = diag
        }

        when(color)
        {
            0 -> dialog.changeToRed()
            1 -> dialog.changeToCyan()
            2 -> dialog.changeToYellow()
            3 -> dialog.changeToPink()
            4 -> dialog.changeToBlue()
            5 -> dialog.changeToGreen()
        }
        if (userName != null)
        {
            dialog.setName(userName)
        }
        set.applyTo(constraintLayout)

    }


  //From 0 to Client 1 from server
    private fun createDialog(message: String, from : Int) : Dialog
    {
        val dialog = Dialog(this.context)
        numsOfDialog++
        dialog.id = numsOfDialog

        dialog.setText(message)


        return dialog
    }
}