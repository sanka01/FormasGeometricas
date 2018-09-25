package br.unitins.formasgeometricas;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TelaPrincipal extends AppCompatActivity {


    //Declara referencia para a superficie de desenho
    private GLSurfaceView superficieDesenho = null;
    //declara referencia para o Render
    private Render render = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Instancia um objeto da superficie de desenho
        superficieDesenho = new GLSurfaceView(this);
        //instancia o objeto renderizador
        render = new Render(this);


        //Configura o objeto de desenho na superficie
        superficieDesenho.setRenderer(render);
        superficieDesenho.setOnTouchListener(render);


        //Publicar a superficie de desenho na tela
        setContentView(superficieDesenho);

    }
}

//Classe que ira implementar a logica do desenho
class Render implements GLSurfaceView.Renderer, View.OnTouchListener, SensorEventListener {

    ArrayList<Geometria> formas = null;

    GL10 gl;
    Quadrado botaoQ = null;
    Triangulo botaoT = null;
    Paralelogramo botaoP = null;
    float largura = 0;
    float altura = 0;
    static final int TAMANHO = 150;

    float sensorX, sensorY, sensorZ;
    long inicioToque;
    AppCompatActivity tela;

    private SensorManager sensor;
    private Sensor acelerometro;
    private List<Sensor> deviceSensors;

    public Render(AppCompatActivity v) {
        this.tela = v;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //define a cor de limpeza no formato RGBA
        gl.glClearColor(0, 0, 0, 1);
        sensor = (SensorManager) tela.getSystemService(Context.SENSOR_SERVICE);

        acelerometro = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        formas = new ArrayList<>();

        botaoP = new Paralelogramo(gl, Render.TAMANHO);
        botaoQ = new Quadrado(gl, Render.TAMANHO);
        botaoT = new Triangulo(gl, Render.TAMANHO);

        //configurando a area de coordenadas do plano cartesiano
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        altura = height;
        largura = width;
        //configurando o volume de renderização
        gl.glOrthof(0, largura,
                0, altura,
                1, -1);

        //configurando a matriz de Transferencias geometricas
        //translação, rotação e escala
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        //configura a area de visualização na tela do DISP
        gl.glViewport(0, 0, width, height);

        //Habilita o desenho por vertices
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        botaoT.setCor(1, 1, 1)
                .setEscala(1, 1)
                .setXY(150, altura - 150)
                .setRotacao(0);

        botaoQ.setCor(1, 1, 1)
                .setEscala(1, 1)
                .setXY(400, altura - 150)
                .setRotacao(0);

        botaoP.setCor(1, 1, 1)
                .setEscala(1, 1)
                .setXY(750, altura - 150)
                .setRotacao(0);


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        this.gl = gl;
        //Aplica a cor de limpeza da tela a todos os bits do buffer de cor
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        botaoT.desenha();
        botaoQ.desenha();
        botaoP.desenha();
        for (Geometria forma : formas) {
            forma.desenha();
            if (naTela(forma))
                forma.setXY(forma.getPosX() - sensorX, forma.getPosY());


        }
    }

    public boolean naTela(Geometria x) {
        if (x.getPosX()  <= largura- x.tamanho && x.getPosX() > 0) {
            Log.i("tela", "dentro");
            return true;
        }

        Log.i("tela", "fora");
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Geometria g = null;
        int flag = 0;
        final float x = event.getX();
        final float y = event.getY();
        float yCalc = altura - y;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                inicioToque = System.currentTimeMillis();
                if (tocar(x, yCalc, botaoP)
                        ) {
                    abrirAlert(new Paralelogramo(gl, TAMANHO), 0, v);
                }

                if (tocar(x, yCalc, botaoQ)
                        ) {
                    abrirAlert(new Quadrado(gl, TAMANHO), 0, v);
                }

                if (tocar(x, yCalc, botaoT)
                        ) {
                    abrirAlert(new Triangulo(gl, TAMANHO), 0, v);
                }

                for (Geometria forma : formas) {

                    forma.selecionado = 0;
                    if (tocar(x, yCalc, forma)
                            ) {
                        forma.selecionado = 1;
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (Geometria forma : formas) {
                    if (tocar(x, yCalc, forma)
                            && forma.selecionado == 1
                            ) {
                        forma.setXY(x, yCalc);
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                for (Geometria forma : formas) {
                    forma.selecionado = 0;
                    if (tocar(x, yCalc, forma)
                            && System.currentTimeMillis() <= inicioToque + 100
                            ) {
                        g = forma;
                        flag = 1;
                        break;
                    }
                }
                if (flag == 1) {
//                    abrirAlert(g,3);
                    formas.remove(g);
                }
                break;
        }

        return true;
    }


    public boolean tocar(float x, float yCalc, Geometria g) {
        if ((x >= (g.getPosX() - g.tamanho / 2)
                && x <= (g.getPosX() + g.tamanho / 2)
        )
                && (yCalc >= (g.getPosY() - g.tamanho / 2)
                && yCalc <= (g.getPosY() + g.tamanho / 2)
        )
                )
            return true;
        return false;

    }

    //O metodo ira abrir uma sequencia de alerts com informações para
    //criar o objeto escolhido
    public void abrirAlert(final Geometria g, int i, final View tela) {

        final EditText angulo = new EditText(tela.getContext());
        angulo.setHint("0");

        final String[] tamanhos = {"0.25", "0.5", "0.75", "1", "1.25", "1.5"};
        final String[] cores = {"Azul", "Vermelho", "Verde", "Amarelo",
                "Magenta", "Ciano", "Branco", "aleatorio"};

        AlertDialog.Builder builder = new AlertDialog.Builder(tela.getContext());

        switch (i) {
            case 0:
                builder.setTitle("Escolha o angulo")
                        .setView(angulo)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                abrirAlert(g, 1, tela);
                                if (!angulo.getText().toString().equals(""))
                                    g.setRotacao(Float.parseFloat(angulo.getText().toString()));
                            }
                        })
                        .setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                break;
            case 1:
                builder.setTitle("Escolha a escala")
                        .setSingleChoiceItems(tamanhos, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                abrirAlert(g, 2, tela);
                                g.setEscala(Float.parseFloat(tamanhos[i]), Float.parseFloat(tamanhos[i]));
                            }
                        });
                break;
            case 2:
                builder.setTitle("Escolha a cor")
                        .setSingleChoiceItems(cores, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                                g.setCor(i);

                                formas.add(g);


                            }
                        });

                break;
            case 3:

                break;

        }
        AlertDialog view = builder.create();
        view.show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        sensorX = event.values[0];
        sensorY = event.values[1];
        sensorZ = event.values[2];
//        Log.i("sensorAcelerometro", "X: " + sensorX + " Y: " + sensorY + " Z: " + sensorZ);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
