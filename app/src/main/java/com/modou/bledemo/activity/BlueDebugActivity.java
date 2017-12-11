package com.modou.bledemo.activity;


import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;


import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.modou.bledemo.R;
import com.modou.bledemo.adapter.BtnsListAdapter;
import com.modou.bledemo.entity.CmdBtnEntity;

import com.modou.bledemo.utils.ConstantUtils;
import com.modou.bledemo.utils.ConvertUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.modou.bledemo.utils.ConstantUtils.DATA_SEND;
import static com.modou.bledemo.utils.ConstantUtils.NB_IOT_HOST;
import static com.modou.bledemo.utils.ConstantUtils.NB_IOT_PORT;
import static com.modou.bledemo.utils.ConstantUtils.isReceiveMsg;

public class BlueDebugActivity extends BaseActivity{

    private TextView tv_title;
    private ImageView iv_back;
    private ImageView iv_icon;
    private Button btn_send;
    private EditText et_cmd;
    private NestedScrollView sv_text;
    private RecyclerView rv_btns;
    private TextView tv_receive_msg;
    private Button btn_select;
    private List<CmdBtnEntity> mData = new ArrayList<>();
    private String manualReturnMessage="";
    private String sendMsg;
    private BtnsListAdapter mBtnsListadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_debug);
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        iv_icon = findViewById(R.id.iv_icon);
        initView();
        initData();
        setLitener();
    }

    public void initView(){
        tv_title.setText(R.string.blue_debug_title);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_send = findViewById(R.id.btn_send);
        et_cmd = findViewById(R.id.et_cmd);
        sv_text = findViewById(R.id.sv_text);
        rv_btns =  findViewById(R.id.rv_btns);
        tv_receive_msg = findViewById(R.id.tv_receive_msg);
        btn_select = findViewById(R.id.btn_select);
        mBtnsListadapter = new BtnsListAdapter(this, mData);
        rv_btns.setLayoutManager(new GridLayoutManager(this, 4));
        rv_btns.setAdapter(mBtnsListadapter);
    }

    @Override
    protected void receiveTextChange(String text) {
        setReturnMsg(text);
    }

    public void setReturnMsg(String str) {
        manualReturnMessage += str;
        tv_receive_msg.setText(manualReturnMessage);
        sv_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sv_text.post(new Runnable() {
                    @Override
                    public void run() {
                        sv_text.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }


    public void setLitener() {
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_cmd.getText().toString() != null && !et_cmd.getText().toString().equals("")) {
                    sendCmd(et_cmd.getText().toString() + "\r\n");
                } else {
                    Toast.makeText(BlueDebugActivity.this, "请输入命令！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBtnsListadapter.setOnItemClickListener(new BtnsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (pos == 2) {
                    isReceiveMsg = !isReceiveMsg;
                    mBtnsListadapter.notifyDataSetChanged();
                } else if (pos == 3) {
                    manualReturnMessage = "";
                    tv_receive_msg.setText(manualReturnMessage);
                } else {
//               发送指令
                    String cmd = mData.get(pos).getCmd();
                    if (cmd.contains("::")) {
                        String[] cmdArray = cmd.split("::");
                        final String firstCmd = cmdArray[0];
                        final String secondCmd = cmdArray[1];
                        sendCmd(firstCmd + "\r\n");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendCmd(secondCmd + "\r\n");
                            }
                        }, 1000);
                    } else {
                        sendCmd(cmd + "\r\n");
                    }
                }
            }
        });
    }

    public void sendCmd(final String str) {
        setReturnMsg(ConvertUtils.getTime() + " 发-->: " + str + "\n");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (str != null && str.length() > 0) {
                    int pakageNum = str.length() / 18 + 1;
                    for (int i = 0; i < pakageNum; i++) {
                        if (i == pakageNum - 1) {
                            sendMsg = str.substring(i * 18, str.length());
                        } else {
                            sendMsg = str.substring(i * 18, i * 18 + 18);
                        }
                        try {
                            bleInstance.write(sendMsg.getBytes("gb2312"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        SystemClock.sleep(250);
                    }
                }
            }
        }).start();
    }

    public void initData() {
        mData.clear();
        for (int i = 0; i < ConstantUtils.CMD_NAME_OF_AT.length; i++) {
            CmdBtnEntity entity = new CmdBtnEntity();
            if (i == 18) {
                entity.setCmd("AT+NSOST=0," + NB_IOT_HOST + "," + NB_IOT_PORT + ",254," + DATA_SEND);
            } else if (i == 23) {
                entity.setCmd("AT+NPING=" + NB_IOT_HOST);
            } else {
                entity.setCmd(ConstantUtils.CMD_OF_AT[i]);
            }
            entity.setCmdName(ConstantUtils.CMD_NAME_OF_AT[i]);
            mData.add(entity);
        }
        mBtnsListadapter.notifyDataSetChanged();
    }

}
